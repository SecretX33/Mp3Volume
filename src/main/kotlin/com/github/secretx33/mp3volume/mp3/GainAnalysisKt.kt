@file:Name("GainAnalysisKt")

package com.github.secretx33.mp3volume.mp3

import com.github.secretx33.mp3volume.meanSquared
import com.github.secretx33.mp3volume.model.ProcessedSample
import com.github.secretx33.mp3volume.model.ProcessingResult
import com.github.secretx33.mp3volume.model.Sample
import com.github.secretx33.mp3volume.readResource
import jdk.jfr.Name
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import java.util.TreeMap
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

/**
 * Source: Replay Gain' [RMS Energy](https://replaygain.hydrogenaud.io/rms_energy.html).
 */
private val SAMPLE_CHUNK_LENGTH = 50.milliseconds

/**
 * Source: Replay Gain' [Statistical Processing](https://replaygain.hydrogenaud.io/statistical_process.html).
 */
private const val RMS_PERCENTILE = 0.95

private val yulewalkCoeffs = readResource<TreeMap<Int, FilterCoefficients>>("coefficients/yulewalk.json")

private val butterworthCoeffs = readResource<TreeMap<Int, FilterCoefficients>>("coefficients/butterworth.json")

/**
 * Given an [audio] stream, calculate the perceived volume of the audio using the `Replay Gain` algorithm.
 *
 * The returned [ProcessingResult] will contain the average perceived volume of the audio, and a list of
 * all the calculated values for each chunk of the audio.
 *
 * The caller is responsible for closing the [Audio].
 */
fun calculatePerceivedVolume(audio: Audio): ProcessingResult {
    val chunkSize = ceil(SAMPLE_CHUNK_LENGTH.inWholeNanoseconds.toDouble() / audio.frameDuration.inWholeNanoseconds.toDouble()).toInt()

    if (log.isTraceEnabled) {
        log.trace("Chunk Size: $chunkSize (${(chunkSize * audio.frameDuration.inWholeNanoseconds).nanoseconds.inWholeMilliseconds}ms)")
    }

    var previousChunk = emptyList<ProcessedSample>()
    val chunkSamples = audio.decodedStream.asAmplitudeValues()
        .chunked(chunkSize)
        .mapIndexed { index, samples ->
            val start = System.nanoTime().nanoseconds

            val loudnessNormalizedSamples = samples.first().indices.map { sampleIndex ->
                val sample = samples.map { it.getOrElse(sampleIndex) { _ -> it[0] } }.toDoubleArray()
                applyLoudnessNormalizeFilters(
                    sample = sample,
                    sampleRate = audio.sampleRate,
                    previousSample = previousChunk.getOrNull(sampleIndex),
                )
            }.also { previousChunk = it }
            val channelsMeanSquared = loudnessNormalizedSamples.map {
                it.processedSample.toList().meanSquared()
            }
            val squaredMeanAverage = channelsMeanSquared.average()
            squaredMeanAverage
//                .also { log.info("${index + 1}. Average: $it (${it.squaredToDecibels()}dB) (${(System.nanoTime().nanoseconds - start).inWholeMicroseconds}mc)") }
        }.toList()

    val sortedChunkSamples = chunkSamples.sorted()
    val rmsPosition = ceil(sortedChunkSamples.size.toDouble() * RMS_PERCENTILE).toInt()
    val rmsValue = sqrt(sortedChunkSamples[rmsPosition])

    return ProcessingResult(
        analysedAudio = audio,
        rmsAverageLoudness = rmsValue,
        rmsAverageLoudnessChunkIndex = rmsPosition,
        samples = sortedChunkSamples,
        chunkSize = chunkSize,
    )
}

/**
 * Transforms the audio [sample] by approximating their values to those perceived by the
 * human ear using Yulewalk and Butterworth IIR filters.
 *
 * See: Replay Gain' [Equal Loudness](https://replaygain.hydrogenaud.io/equal_loudness.html).
 */
private fun applyLoudnessNormalizeFilters(
    sample: Sample,
    sampleRate: Int,
    previousSample: ProcessedSample? = null,
): ProcessedSample {
    val yulewalkCoeffs = yulewalkCoeffs.getClosest(sampleRate)
    val butterworthCoeffs = butterworthCoeffs.getClosest(sampleRate)
    val lookbehindAmount = max(yulewalkCoeffs.size, butterworthCoeffs.size)

    val preparedSample = (previousSample?.sample?.takeLast(lookbehindAmount)?.toDoubleArray() ?: doubleArrayOf()) + sample

    // Apply Yulewalk and Butterworth filters
    val filteredYulewalk = applyIIRFilter(preparedSample, yulewalkCoeffs)
    val filteredButterworth = applyIIRFilter(filteredYulewalk, butterworthCoeffs)

    val processedSample = when {
        filteredButterworth.size > sample.size -> filteredButterworth.drop(filteredButterworth.size - sample.size).toDoubleArray()
        else -> filteredButterworth
    }
    return ProcessedSample(sample, processedSample)
}

/**
 * Apply an IIR (Infinite Impulse Response) filter, returning a copy of the `input` array.
 */
private fun applyIIRFilter(input: Sample, coeffs: FilterCoefficients): DoubleArray {
    val output = DoubleArray(input.size)
    val bufferSize = coeffs.size
    val a = coeffs.a
    val b = coeffs.b

    input.indices.forEach { n ->
        var sumA = 0.0
        for (i in 1 until min(bufferSize, n)) {
            sumA += a[i] * output[n - i]
        }

        var sumB = 0.0
        for (i in 0 until min(bufferSize, n + 1)) {
            sumB += b[i] * input[n - i]
        }

        val result = sumB - sumA
        require(result.isFinite()) { "Somehow, the applying IIR filter has return a bogus value ($result), there's something very wrong in this code or the input" }

        output[n] = result
    }

    return output
}

private data class FilterCoefficients(val a: List<Double>, val b: List<Double>) {
    init {
        require(a.size == b.size) { "Filter coefficients must have the same size, a = ${a.size}, b = ${b.size}" }
    }
    val size = a.size
}

/**
 * Retrieves a value from the [TreeMap] that is closest to the given [key].
 *
 * @throws NoSuchElementException if the map is empty.
 */
@Suppress("UNCHECKED_CAST")
private fun <K : Comparable<K>, V> TreeMap<K, V>.getClosest(key: K): V {
    val closestKey = setOfNotNull(floorKey(key), ceilingKey(key)).minBy(key::compareTo)
    return get(closestKey) as V
}
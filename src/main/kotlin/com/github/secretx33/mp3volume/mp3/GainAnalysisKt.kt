@file:Name("GainAnalysisKt")

package com.github.secretx33.mp3volume.mp3

import com.github.secretx33.mp3volume.meanSquared
import com.github.secretx33.mp3volume.microsElapsedUntilNow
import com.github.secretx33.mp3volume.model.ProcessedSample
import com.github.secretx33.mp3volume.model.ProcessingResult
import com.github.secretx33.mp3volume.model.SampleArray
import com.github.secretx33.mp3volume.model.SamplesArray
import com.github.secretx33.mp3volume.model.SamplesList
import com.github.secretx33.mp3volume.model.toSampleArray
import com.github.secretx33.mp3volume.readResource
import com.github.secretx33.mp3volume.squaredToDecibels
import jdk.jfr.Name
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import java.util.TreeMap
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

private val yulewalkCoeffs = readResource<TreeMap<Int, FilterCoefficients>>("coefficients/yulewalk.json")

private val butterworthCoeffs = readResource<TreeMap<Int, FilterCoefficients>>("coefficients/butterworth.json")

/**
 * Source: Replay Gain' [Statistical Processing](https://replaygain.hydrogenaud.io/statistical_process.html).
 */
private const val RMS_PERCENTILE = 0.95

/**
 * Given an [audio] stream, calculate the perceived volume of the audio using the `Replay Gain` algorithm.
 *
 * The returned [ProcessingResult] will contain the average perceived volume of the audio, and a list of
 * all the calculated values for each chunk of the audio.
 *
 * The caller is responsible for closing the [Audio].
 */
fun calculatePerceivedVolume(audio: Audio): ProcessingResult {
    val isTraceEnabled = log.isTraceEnabled
    if (isTraceEnabled) {
        log.trace("Chunk Size: ${audio.chunkSize} (${audio.chunkDuration.inWholeMilliseconds}ms)")
    }

    var previousChunk = emptyList<ProcessedSample>()
    val chunkSamples = audio.decodedStream.asAmplitudeValues()
        .chunked(audio.chunkSize)
        .mapIndexed { index, samples ->
            val start = System.nanoTime()

            val loudnessNormalizedSamples = samples.first().indices.map { sampleIndex ->
                val sample = samples.map { it.getOrElse(sampleIndex) { _ -> it[0] } }
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

            if (isTraceEnabled) {
                log.trace("${index + 1}. Average: $squaredMeanAverage (${squaredMeanAverage.squaredToDecibels()}dB) (${microsElapsedUntilNow(start)}µ)")
            }
            squaredMeanAverage
        }.toList().sorted()

    val rmsPosition = ceil(chunkSamples.size.toDouble() * RMS_PERCENTILE).toInt()
    val rmsValue = sqrt(chunkSamples[rmsPosition])

    return ProcessingResult(
        analysedAudio = audio,
        rmsAverageLoudness = rmsValue,
        rmsAverageLoudnessChunkIndex = rmsPosition,
        samples = chunkSamples,
    )
}

/**
 * Transforms the audio [sample] by approximating their values to those perceived by the
 * human ear using Yulewalk and Butterworth IIR filters.
 *
 * See: Replay Gain' [Equal Loudness](https://replaygain.hydrogenaud.io/equal_loudness.html).
 */
private fun applyLoudnessNormalizeFilters(
    sample: SamplesList,
    sampleRate: Int,
    previousSample: ProcessedSample? = null,
): ProcessedSample {
    val yulewalkCoeffs = yulewalkCoeffs.getClosest(sampleRate)
    val butterworthCoeffs = butterworthCoeffs.getClosest(sampleRate)
    val lookbehindAmount = max(yulewalkCoeffs.size, butterworthCoeffs.size)

    val preparedSample = previousSample?.sample?.takeLast(lookbehindAmount).orEmpty().toSampleArray() + sample

    // Apply Yulewalk and Butterworth filters
    val filteredYulewalk = applyIIRFilter(preparedSample, yulewalkCoeffs)
    val filteredButterworth = applyIIRFilter(filteredYulewalk, butterworthCoeffs)

    val processedSample = when {
        filteredButterworth.size > sample.size -> filteredButterworth.drop(filteredButterworth.size - sample.size)
        else -> filteredButterworth.toList()
    }
    return ProcessedSample(sample, processedSample)
}

/**
 * Apply an IIR (Infinite Impulse Response) filter, returning a copy of the `input` array.
 */
private fun applyIIRFilter(input: SamplesArray, coeffs: FilterCoefficients): SamplesArray {
    val output = SampleArray(input.size)
    val bufferSize = coeffs.size
    val a = coeffs.a
    val b = coeffs.b

    input.indices.forEach { n ->
        var sumA = 0f
        for (i in 1 until min(bufferSize, n)) {
            sumA += a[i] * output[n - i]
        }

        var sumB = 0f
        for (i in 0 until min(bufferSize, n + 1)) {
            sumB += b[i] * input[n - i]
        }

        val result = sumB - sumA
        require(result.isFinite()) { "Somehow, the applying IIR filter has return a bogus value ($result), there's something very wrong in this code or the input" }

        output[n] = result
    }

    return output
}

/**
 * Wrapper class for filter coefficient values.
 *
 * Note that the [a] and [b] arrays are supposed to be immutable, but using `Array` instead of `List` gives a `7%`
 * bump in performance, hence why we are using `Array` instead of an immutable `List`.
 *
 * So, **do not mutate the arrays**.
 */
private class FilterCoefficients(val a: SamplesArray, val b: SamplesArray) {
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
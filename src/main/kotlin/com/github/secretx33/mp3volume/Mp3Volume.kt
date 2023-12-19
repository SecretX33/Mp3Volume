@file:Suppress("RemoveExplicitTypeArguments", "UnstableApiUsage", "RedundantSuspendModifier", "UNCHECKED_CAST")
@file:OptIn(ExperimentalTime::class, ExperimentalPathApi::class)

package com.github.secretx33.mp3volume

import com.github.secretx33.mp3volume.model.ProcessedSample
import com.github.secretx33.mp3volume.mp3.Audio
import com.github.secretx33.mp3volume.mp3.GainAnalysis
import com.github.secretx33.mp3volume.mp3.ReplayGain
import com.github.secretx33.mp3volume.mp3.applyLoudnessNormalizeFilters
import com.github.secretx33.mp3volume.mp3.normalizedSamplesSequence
import com.github.secretx33.mp3volume.mp3.readMp3WithDefaults
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.ExperimentalTime

private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

fun main(args: Array<String>) {
    val folder = Path("E:\\testAudios")
    folder.listDirectoryEntries("*.mp3")
        .sortedBy { it.name }
        .forEach(::processFile)
}

private fun processFile(file: Path) {
    val start = System.nanoTime()
    try {
        readMp3WithDefaults(file).use { audio ->
            val frameDuration = audio.frameDuration
            val chunkSize = ceil(50.milliseconds.inWholeNanoseconds.toDouble() / frameDuration.inWholeNanoseconds.toDouble()).toInt()

//            log.info("Chunk Size: $chunkSize (${(chunkSize * frameDuration.inWholeNanoseconds).nanoseconds.inWholeMilliseconds}ms)")

//            readAudioWithMp3GainImplementationOfReplayGain(audio, chunkSize)
            readAudioWithMyImplementationOfReplayGain(audio, chunkSize, start, file.name)
        }
    } catch (e: Throwable) {
        log.error("Error calculating the perceived volume of '$file' (after ${millisElapsedUntilNow(start)}ms", e)
    }
}

private fun readAudioWithMp3GainImplementationOfReplayGain(
    audio: Audio,
    chunkSize: Int,
) {
    val replayGain = ReplayGain()
    val gainAnalysis = GainAnalysis().apply {
        InitGainAnalysis(replayGain, audio.sampleRate.toLong())
    }

    audio.stream.normalizedSamplesSequence().map { it.map { it.toFloat() } }
        .chunked(chunkSize)
        .forEach {
            val start = System.nanoTime()
            gainAnalysis.AnalyzeSamples(
                replayGain,
                it.map { it[0] }.toFloatArray(),
                0,
                it.map { it.getOrElse(1) { _ -> it[0] } }.toFloatArray(),
                0,
                it.size,
                2,
            )
            log.info("Analyzed ${it.size} samples in ${(System.nanoTime() - start).nanoseconds.inWholeMicroseconds}mc")
        }

    val titleGain = gainAnalysis.GetTitleGain(replayGain)
    log.info("Replay Gain: $titleGain (${titleGain.toDouble().toDecibels()}dB)")
}

private fun readAudioWithMyImplementationOfReplayGain(
    audio: Audio,
    chunkSize: Int,
    start: Long,
    fileName: String,
) {
    var previousChunk = emptyList<ProcessedSample>()
    val chunkSamples = audio.stream.normalizedSamplesSequence()
        .chunked(chunkSize)
        .mapIndexed { index, samples ->
            val start = System.nanoTime().nanoseconds

            val loudnessNormalizedSamples = samples.first().indices.map { sampleIndex ->
                val sample = samples.map { it.getOrElse(sampleIndex) { _ -> it[0] } }.toDoubleArray()
                applyLoudnessNormalizeFilters(
                    sample = sample,
                    sampleRate = audio.sampleRate,
                    previousChunk = previousChunk.getOrNull(sampleIndex),
                )
            }.also { previousChunk = it }
            val channelsMeanSquared = loudnessNormalizedSamples.map {
                it.processedSample.toList().meanSquared()
            }
            val meanAverage = channelsMeanSquared.average()
            meanAverage
//                .also { log.info("${index + 1}. Average: $it (${it.squaredToDecibels()}dB) (${(System.nanoTime().nanoseconds - start).inWholeMicroseconds}mc)") }
        }.toList()
    val sortedChunkSamples = chunkSamples.sorted()
    val rmsPosition = ceil(sortedChunkSamples.size.toDouble() * 0.95).toInt()
    val rmsValue = sortedChunkSamples[rmsPosition]

//    printDetailedSummary(sortedChunkSamples, rmsValue, start, fileName)
    printSimpleSummary(rmsValue, start, fileName)
}

private fun printDetailedSummary(
    sortedChunkSamples: List<Double>,
    rmsValue: Double,
    start: Long,
    fileName: String,
) = log.info("""
    '$fileName' Total Samples: ${sortedChunkSamples.size} (in ${millisElapsedUntilNow(start)}ms)

    Min: ${sortedChunkSamples.first()} (${sortedChunkSamples.first().squaredToDecibels()}dB)
    Max: ${sortedChunkSamples.last()} (${sortedChunkSamples.last().squaredToDecibels()}dB)

    Median: ${sortedChunkSamples[sortedChunkSamples.size / 2]} (${sortedChunkSamples[sortedChunkSamples.size / 2].squaredToDecibels()}dB)
    Mean: ${sortedChunkSamples.average()} (${sortedChunkSamples.average().squaredToDecibels()}dB)
    RMS: ${sortedChunkSamples.rootMeanSquared()} (${sortedChunkSamples.rootMeanSquared().toDecibels()}dB)

    ReplayGain: $rmsValue (${rmsValue.squaredToDecibels().formattedDecimal()}dB)
""".trimIndent())

private fun printSimpleSummary(
    rmsValue: Double,
    start: Long,
    fileName: String,
) = log.info("$fileName -> Volume: $rmsValue (${rmsValue.squaredToDecibels().formattedDecimal()}dB) in ${millisElapsedUntilNow(start)}ms")

@file:Suppress("RemoveExplicitTypeArguments", "UnstableApiUsage", "RedundantSuspendModifier", "UNCHECKED_CAST")
@file:OptIn(ExperimentalTime::class, ExperimentalPathApi::class)

package com.github.secretx33.mp3volume

import com.github.secretx33.mp3volume.mp3.Audio
import com.github.secretx33.mp3volume.mp3.GainAnalysis
import com.github.secretx33.mp3volume.mp3.ReplayGain
import com.github.secretx33.mp3volume.mp3.applyLoudnessNormalizeFilters
import com.github.secretx33.mp3volume.mp3.meanSquared
import com.github.secretx33.mp3volume.mp3.normalizedSamplesSequence
import com.github.secretx33.mp3volume.mp3.readMp3WithDefaults
import com.github.secretx33.mp3volume.mp3.rootMeanSquared
import com.github.secretx33.mp3volume.mp3.toDecibels
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.ExperimentalTime

private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

fun main(args: Array<String>) {
    val start = System.nanoTime().nanoseconds

    try {
        val file = Path("E:\\000f53b5640ccfa1a6a7bedd289566aa.mp3")

        readMp3WithDefaults(file).use { audio ->
            val frameDuration = audio.frameDuration
            val chunkSize = ceil(50.milliseconds.inWholeNanoseconds.toDouble() / frameDuration.inWholeNanoseconds.toDouble()).toInt()

            log.info("Chunk Size: $chunkSize (${(chunkSize * frameDuration.inWholeNanoseconds).nanoseconds.inWholeMilliseconds}ms)")

//            readAudioWithMp3GainImplementationOfReplayGain(audio, chunkSize)
            readAudioWithMyImplementationOfReplayGain(audio, chunkSize, start)

            println()
        }

    } catch (e: Throwable) {
        log.error("Error executing this script (after ${(System.nanoTime().nanoseconds - start).inWholeMilliseconds}ms)!", e)
    }
}

private fun readAudioWithMp3GainImplementationOfReplayGain(
    audio: Audio,
    chunkSize: Int,
) {
    val replayGain = ReplayGain()
    val gainAnalysis = GainAnalysis().apply {
        InitGainAnalysis(replayGain, audio.audioFormat.sampleRate.toLong())
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
    start: Duration,
) {
    val chunkSamples = audio.stream.normalizedSamplesSequence()
        .chunked(chunkSize)
        .mapIndexed { index, samples ->
            val start = System.nanoTime().nanoseconds

            val loudnessNormalizedSamples = samples.first().indices.map { sampleIndex ->
                applyLoudnessNormalizeFilters(
                    samples.mapNotNull { it.getOrNull(sampleIndex) }.toDoubleArray(),
                    audio.audioFormat.sampleRate.toInt()
                )
            }
            val channelsMeanSquared = loudnessNormalizedSamples.map {
                it.toList().meanSquared()
            }
            val meanAverage = channelsMeanSquared.average()
            meanAverage
                .also { log.info("${index + 1}. Average: $it (${it.toDecibels()}dB) (${(System.nanoTime().nanoseconds - start).inWholeMicroseconds}mc)") }
        }.toList()
    val sortedChunkSamples = chunkSamples.sorted()

    log.info("""
        Total Samples: ${chunkSamples.size} (in ${(System.nanoTime().nanoseconds - start).inWholeMilliseconds}ms)

        Min: ${sortedChunkSamples.first()} (${sortedChunkSamples.first().toDecibels()}dB)
        Max: ${sortedChunkSamples.last()} (${sortedChunkSamples.last().toDecibels()}dB)

        Median: ${sortedChunkSamples[sortedChunkSamples.size / 2]} (${sortedChunkSamples[sortedChunkSamples.size / 2].toDecibels()}dB)
        Mean: ${sortedChunkSamples.average()} (${sortedChunkSamples.average().toDecibels()}dB)
        RMS: ${sortedChunkSamples.rootMeanSquared()} (${sortedChunkSamples.rootMeanSquared().toDecibels()}dB)

        ReplayGain: ${sortedChunkSamples[floor(sortedChunkSamples.size.toDouble() * 0.95).toInt()]} (${sortedChunkSamples[floor(sortedChunkSamples.size.toDouble() * 0.95).toInt()].toDecibels()}dB)
    """.trimIndent())
}

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
import com.github.secretx33.mp3volume.mp3.squaredToDecibels
import com.github.secretx33.mp3volume.mp3.toDecibels
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.ExperimentalTime

private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

fun main(args: Array<String>) {
    val start = System.nanoTime().nanoseconds

    val file = Path("E:\\Saradominists - RuneScape 3 Music.mp3")

    try {
        readMp3WithDefaults(file).use { audio ->
            val frameDuration = audio.frameDuration
            val chunkSize = ceil(50.milliseconds.inWholeNanoseconds.toDouble() / frameDuration.inWholeNanoseconds.toDouble()).toInt()

            log.info("Chunk Size: $chunkSize (${(chunkSize * frameDuration.inWholeNanoseconds).nanoseconds.inWholeMilliseconds}ms)")

//            readAudioWithMp3GainImplementationOfReplayGain(audio, chunkSize)
            readAudioWithMyImplementationOfReplayGain(audio, chunkSize, start)

            println()
        }

    } catch (e: Throwable) {
        log.error("Error calculating the perceived volume of '$file' (after ${(System.nanoTime().nanoseconds - start).inWholeMilliseconds}ms)", e)
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
    start: Duration,
) {
    val chunkSamples = audio.stream.normalizedSamplesSequence()
        .chunked(chunkSize)
        .mapIndexed { index, samples ->
            val start = System.nanoTime().nanoseconds

            val loudnessNormalizedSamples = samples.first().indices.map { sampleIndex ->
                applyLoudnessNormalizeFilters(
                    samples.map { it.getOrElse(sampleIndex) { _ -> it[0] } }.toDoubleArray(),
                    audio.sampleRate,
                )
            }
            val channelsMeanSquared = loudnessNormalizedSamples.map {
                it.toList().meanSquared()
            }
            val meanAverage = channelsMeanSquared.average()
            meanAverage
                .also { log.info("${index + 1}. Average: $it (${it.squaredToDecibels()}dB) (${(System.nanoTime().nanoseconds - start).inWholeMicroseconds}mc)") }
        }.toList()
    val sortedChunkSamples = chunkSamples.sorted()
    val rmsPosition = ceil(sortedChunkSamples.size.toDouble() * 0.95).toInt()
    val rmsValue = sortedChunkSamples[rmsPosition]

    log.info("""
        Total Samples: ${chunkSamples.size} (in ${(System.nanoTime().nanoseconds - start).inWholeMilliseconds}ms)

        Min: ${sortedChunkSamples.first()} (${sortedChunkSamples.first().squaredToDecibels()}dB)
        Max: ${sortedChunkSamples.last()} (${sortedChunkSamples.last().squaredToDecibels()}dB)

        Median: ${sortedChunkSamples[sortedChunkSamples.size / 2]} (${sortedChunkSamples[sortedChunkSamples.size / 2].squaredToDecibels()}dB)
        Mean: ${sortedChunkSamples.average()} (${sortedChunkSamples.average().squaredToDecibels()}dB)
        RMS: ${sortedChunkSamples.rootMeanSquared()} (${sortedChunkSamples.rootMeanSquared().toDecibels()}dB)

        ReplayGain: $rmsValue (${rmsValue.squaredToDecibels()}dB)
    """.trimIndent())
}

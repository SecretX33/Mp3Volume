@file:Suppress("RemoveExplicitTypeArguments", "UnstableApiUsage", "RedundantSuspendModifier", "UNCHECKED_CAST")
@file:OptIn(ExperimentalTime::class, ExperimentalPathApi::class)

package com.github.secretx33.mp3volume

import com.github.secretx33.mp3volume.mp3.applyLoudnessNormalizeFilter
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.invoke.MethodHandles
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.io.path.ExperimentalPathApi
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

fun main(args: Array<String>) {
    val start = System.nanoTime().nanoseconds

    try {
        val file = File("E:\\untitled.mp3")
        AudioSystem.getAudioInputStream(file).use { rawInput ->
            val baseFormat = rawInput.format
            val decodedFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.sampleRate.coerceAtMost(96000f),
                16,
                baseFormat.channels,
                baseFormat.channels * 2,
                baseFormat.sampleRate,
                false,
            )

            AudioSystem.getAudioInputStream(decodedFormat, rawInput).use {
                val frameDuration = decodedFormat.frameDuration
                val chunkSize = ceil(50.milliseconds.inWholeNanoseconds.toDouble() / frameDuration.inWholeNanoseconds.toDouble()).toInt()

                log.info("Chunk Size: $chunkSize (${(chunkSize * frameDuration.inWholeNanoseconds).nanoseconds.inWholeMilliseconds}ms)")

//                val replayGain = ReplayGain()
//                val gainAnalysis = GainAnalysis().apply {
//                    InitGainAnalysis(replayGain, decodedFormat.sampleRate.toLong())
//                }
//
//                it.frameSequence().map { it.frameToNormalizedSamples().map { it.toFloat() } }
//                    .chunked(chunkSize)
//                    .forEach {
//                        val start = System.nanoTime()
//                        gainAnalysis.AnalyzeSamples(
//                            replayGain,
//                            it.map { it[0] }.toFloatArray(),
//                            0,
//                            it.map { it.getOrElse(1) { _ -> it[0] } }.toFloatArray(),
//                            0,
//                            it.size,
//                            2,
//                        )
//                        log.info("Analyzed ${it.size} samples in ${(System.nanoTime() - start).nanoseconds.inWholeMicroseconds}mc")
//                    }
//
//                val titleGain = gainAnalysis.GetTitleGain(replayGain)
//                log.info("Replay Gain: $titleGain (${titleGain.toDouble().toDecibels()}dB)")

                val chunkSamples = it.frameSequence().map { it.frameToNormalizedSamples() }
                    .chunked(chunkSize)
                    .mapIndexed { index, samples ->
                        val start = System.nanoTime().nanoseconds

                        val loudnessNormalizedSamples = samples.first().indices.map { sampleIndex ->
                            applyLoudnessNormalizeFilter(samples.mapNotNull { it.getOrNull(sampleIndex) }.toDoubleArray(), decodedFormat.sampleRate.toInt())
                        }
                        val channelsMeanSquared = loudnessNormalizedSamples.map {
                            it.toList().meanSquared()
                        }
                        val rootMeanAverage = sqrt(channelsMeanSquared.average())
                        rootMeanAverage
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

                println()
            }
        }
    } catch (e: Throwable) {
        log.error("Error executing this script (after ${(System.nanoTime().nanoseconds - start).inWholeMilliseconds}ms)!", e)
    }
}

fun Iterable<Double>.meanSquared(): Double = map { it.pow(2) }.average()

fun Iterable<Double>.rootMeanSquared(): Double = sqrt(meanSquared())

fun Double.toDecibels(): Double = 10 * log10(this + 10e-10)

fun ByteArray.frameToNormalizedSamples(): List<Double> {
    require(size % 2 == 0) { "Frame size must be multiple of 2, but $size is not" }
    val samples = (0..lastIndex step 2).map {
        val sample = ByteBuffer.wrap(this, it, 2).order(ByteOrder.LITTLE_ENDIAN).getShort()
        (sample.toDouble() / 32767.0).coerceIn(-1.0, 1.0)
    }
    return samples
}

fun AudioInputStream.frameSequence(): Sequence<ByteArray> = sequence {
    var readBytes: Int
    var buffer = ByteArray(format.frameSize)
    val buffered = buffered(DEFAULT_BUFFER_SIZE)

    while (buffered.read(buffer).also { readBytes = it } > 0) {
        if (readBytes != buffer.size) {
            buffer = buffer.copyOf(readBytes)
        }

        yield(buffer)

        if (format.frameSize != buffer.size) {
            buffer = ByteArray(format.frameSize)
        }
    }
}

val AudioFormat.frameDuration: Duration get() = (1.0 / sampleRate.toDouble()).seconds
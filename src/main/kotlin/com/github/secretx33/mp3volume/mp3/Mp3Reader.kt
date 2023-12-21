package com.github.secretx33.mp3volume.mp3

import com.github.secretx33.mp3volume.asSequence
import com.github.secretx33.mp3volume.frameDuration
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.time.Duration

/**
 * Read an MP3 file and return an [Audio] object that wraps both source and decoded `AudioInputStream`s .
 *
 * The returned audio stream will always have these properties:
 *
 * - Sample rate will be the same as the source, but no higher than `96kHz`.
 * - Sample size will be `16` bits.
 * - Channels will be the same as the source.
 * - Encoding will be `PCM_SIGNED`.
 *
 * The caller is responsible for closing the [Audio] object after it's no longer needed.
 */
fun readMp3WithDefaults(file: Path): Audio {
    val sourceStream = AudioSystem.getAudioInputStream(file.toFile())
    val sourceFormat: AudioFormat = sourceStream.format
    val targetFormat = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        sourceFormat.sampleRate.coerceAtMost(96000f),
        16,
        sourceFormat.channels,
        sourceFormat.channels * 2,
        sourceFormat.sampleRate,
        false,
    )
    val targetStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream)

    return Audio(
        file = file,
        sourceStream = sourceStream,
        decodedStream = targetStream,
    )
}

/**
 * Return a sequence that abstracts the reading and parsing of the audio stream into frames, and the
 * mapping of those frames into normalized samples.
 *
 * The caller is responsible for closing the [AudioInputStream] after the sequence is consumed.
 */
fun AudioInputStream.normalizedSamplesSequence(): Sequence<List<Double>> = framesSequence()
    .map { it.frameToNormalizedSamples() }

private fun AudioInputStream.framesSequence(): Sequence<ByteArray> = buffered()
    .asSequence(chunkSize = format.frameSize)
    .filter { it.size == format.frameSize }

/**
 * Maps an audio frame into a normalized sample.
 *
 * Each item of the list represents a frame of an audio channel that was normalized into `-1.0 ~ 1.0`
 * amplitude.
 */
private fun ByteArray.frameToNormalizedSamples(): List<Double> {
    require(size % 2 == 0) { "Frame size must be multiple of 2, but $size is not" }
    val samples = (0..lastIndex step 2).map {
        val sample = ByteBuffer.wrap(this, it, 2).order(ByteOrder.LITTLE_ENDIAN).getShort()
        val value = (sample.toDouble() / 32767.0).coerceIn(-1.0, 1.0)
        value
    }
    return samples
}

@Suppress("MemberVisibilityCanBePrivate")
class Audio(
    val file: Path,
    private val sourceStream: AudioInputStream,
    val decodedStream: AudioInputStream,
) : Closeable {
    val audioFormat: AudioFormat = decodedStream.format
    val sampleRate: Int = audioFormat.sampleRate.toInt()
    val frameDuration: Duration = audioFormat.frameDuration

    override fun close() {
        val failures = listOf(sourceStream, decodedStream)
            .map { runCatching(it::close) }
            .filter { it.isFailure }

        failures.reduceOrNull { acc, result ->
            acc.also { it.exceptionOrNull()!!.addSuppressed(result.exceptionOrNull()!!) }
        }?.let { throw it.exceptionOrNull()!! }
    }
}
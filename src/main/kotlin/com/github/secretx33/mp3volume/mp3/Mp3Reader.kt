package com.github.secretx33.mp3volume.mp3

import com.github.secretx33.mp3volume.asSequence
import com.github.secretx33.mp3volume.frameDuration
import com.github.secretx33.mp3volume.model.SamplesList
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.io.path.name
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

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
        audioStream = targetStream,
    )
}

/**
 * Return a sequence that abstracts the reading and parsing of the audio stream into frames, and the
 * mapping of these frames into absolute amplitude values (ranging from `-1.0` to `1.0`).
 *
 * The caller is responsible for closing the [AudioInputStream] after the sequence is consumed.
 */
fun AudioInputStream.asAmplitudeValues(): Sequence<SamplesList> = framesSequence()
    .map { it.frameToAmplitudeValues() }

/**
 * The caller is responsible for closing the [AudioInputStream] after the sequence is consumed.
 */
private fun AudioInputStream.framesSequence(): Sequence<ByteArray> = buffered()
    .asSequence(chunkSize = format.frameSize)
    .filter { it.size == format.frameSize }

/**
 * Maps an audio frame into its amplitude value.
 *
 * Each item of the list represents a frame of an audio channel that was converted into an
 * amplitude ranging from `-1.0` to `1.0`.
 */
private fun ByteArray.frameToAmplitudeValues(): SamplesList {
    require(size % 2 == 0) { "Frame size must be multiple of 2, but $size is not" }
    val samples = (0..lastIndex step 2).map {
        val sample = ByteBuffer.wrap(this, it, 2).order(ByteOrder.LITTLE_ENDIAN).getShort()
        val value = (sample.toFloat() / 32767f).coerceIn(-1f, 1f)
        value
    }
    return samples
}

@Suppress("MemberVisibilityCanBePrivate")
class Audio(
    val file: Path,
    val audioStream: AudioInputStream,
) : Closeable {
    val audioFormat: AudioFormat = audioStream.format
    val sampleRate: Int = audioFormat.sampleRate.toInt()
    val bitDepth: Int = audioFormat.sampleSizeInBits
    val maxAmplitude: Int = 2.0.pow(bitDepth - 1).toInt() - 1
    val frameDuration: Duration = audioFormat.frameDuration
    val chunkSize: Int = ceil(SAMPLE_CHUNK_LENGTH.inWholeNanoseconds.toDouble() / frameDuration.inWholeNanoseconds.toDouble()).toInt()
    val chunkDuration: Duration = (chunkSize * frameDuration.inWholeNanoseconds).nanoseconds

    init {
        // Sanity checks
        require(sampleRate > 0) { "Sample rate must be greater than 0 (actual: $sampleRate)" }
        require(bitDepth > 0) { "Bit depth must be greater than 0 (actual: $bitDepth)" }
        require(maxAmplitude > 0) { "Max amplitude must be greater than 0 (actual: $maxAmplitude)" }
        require(frameDuration > Duration.ZERO) { "Frame duration must be greater than 0 (actual: $frameDuration)" }
        require(chunkSize > 0) { "Chunk size must be greater than 0 (actual: $chunkSize)" }
        require(chunkDuration > Duration.ZERO) { "Chunk duration must be greater than 0 (actual: $chunkDuration)" }
    }

    override fun close() = audioStream.close()

    override fun toString(): String = "Audio(${file.name}, $bitDepth-bit, sampleRate=$sampleRate, maxAmplitude=$maxAmplitude)"

}
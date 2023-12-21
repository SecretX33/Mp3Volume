@file:Suppress("RemoveExplicitTypeArguments", "UnstableApiUsage", "RedundantSuspendModifier", "UNCHECKED_CAST")
@file:OptIn(ExperimentalTime::class, ExperimentalPathApi::class)

package com.github.secretx33.mp3volume

import com.github.secretx33.mp3volume.model.ProcessingResult
import com.github.secretx33.mp3volume.model.SampleUnit
import com.github.secretx33.mp3volume.mp3.Audio
import com.github.secretx33.mp3volume.mp3.GainAnalysis
import com.github.secretx33.mp3volume.mp3.ReplayGain
import com.github.secretx33.mp3volume.mp3.calculatePerceivedVolume
import com.github.secretx33.mp3volume.mp3.normalizedSamplesSequence
import com.github.secretx33.mp3volume.mp3.readMp3WithDefaults
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.ExperimentalTime

private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

fun main(args: Array<String>) {
    val folder = Path("E:\\testAudios")
//    listOf(folder.listDirectoryEntries("*.mp3").sortedBy { it.name }.filter { it.name.startsWith("Song") }.first())
    folder.listDirectoryEntries("*.mp3").sortedByDescending { it.name }.drop(1)
        .forEach {
//            readId3Tag(it)
            processFile(it)
        }
}

private fun processFile(file: Path) {
    val start = System.nanoTime()
    try {
        readMp3WithDefaults(file).use { audio ->
//            readAudioWithMp3GainImplementationOfReplayGain(audio, chunkSize)
            readAudioWithMyImplementationOfReplayGain(audio, start)
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

    audio.decodedStream.normalizedSamplesSequence().map { it.map { it.toFloat() } }
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
    start: Long,
) {
    val processingResult = calculatePerceivedVolume(audio)

//    printDetailedSummary(sortedChunkSamples, rmsValue, start, fileName)
    printSimpleSummary(processingResult, start)
}

private fun printDetailedSummary(
    sortedChunkSamples: List<SampleUnit>,
    rmsValue: SampleUnit,
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
    processingResult: ProcessingResult,
    start: Long,
) {
    val fileName = processingResult.analysedAudio.file.name
    val averageLoudness = processingResult.rmsAverageLoudness
    val decibelsFs = averageLoudness.toDecibels()
    log.info("$fileName -> Volume: $averageLoudness (${decibelsFs.formattedDecimal()}dBFS, ${(decibelsFs + 113.0).formattedDecimal()}dB) in ${millisElapsedUntilNow(start)}ms")
}

@file:Suppress("RemoveExplicitTypeArguments", "UnstableApiUsage", "RedundantSuspendModifier", "UNCHECKED_CAST")
@file:OptIn(ExperimentalTime::class, ExperimentalPathApi::class)

package com.github.secretx33.mp3volume

import com.github.secretx33.mp3volume.model.ProcessingResult
import com.github.secretx33.mp3volume.mp3.Audio
import com.github.secretx33.mp3volume.mp3.GainAnalysis
import com.github.secretx33.mp3volume.mp3.ReplayGain
import com.github.secretx33.mp3volume.mp3.asAmplitudeValues
import com.github.secretx33.mp3volume.mp3.calculatePerceivedVolume
import com.github.secretx33.mp3volume.mp3.readMp3WithDefaults
import org.slf4j.LoggerFactory
import java.lang.String.CASE_INSENSITIVE_ORDER
import java.lang.invoke.MethodHandles
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.ExperimentalTime

private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

fun main(args: Array<String>) {
    val folder = Path("E:\\testAudios")
//    listOf(folder.listDirectoryEntries("*.mp3").sortedBy { it.name }.filter { it.name.startsWith("Song") }.first())
    folder.listDirectoryEntries("*.mp3").sortedWith(compareBy<Path, String>(CASE_INSENSITIVE_ORDER) { it.name })
        .forEach {
//            readId3Tag(it)
            processFile(it)
        }
}

private fun processFile(file: Path) {
    var start = System.nanoTime()
    try {
        readMp3WithDefaults(file).use { audio -> readAudioWithMyImplementationOfReplayGain(audio, start) }

//        start = System.nanoTime()
//        readMp3WithDefaults(file).use { audio -> readAudioWithMp3GainImplementationOfReplayGain(audio, start) }
//        start = System.nanoTime()
//        readMp3WithDefaults(file).use { audio -> readAudioWithMp3GainImplementationOfReplayGain(audio, start) }
    } catch (e: Throwable) {
        log.error("Error calculating the perceived volume of '$file' (after ${millisElapsedUntilNow(start)}ms", e)
    }
}

private fun readAudioWithMp3GainImplementationOfReplayGain(
    audio: Audio,
    start: Long,
) {
    val replayGain = ReplayGain()
    val gainAnalysis = GainAnalysis().apply {
        InitGainAnalysis(replayGain, audio.sampleRate.toLong())
    }

    audio.decodedStream.asAmplitudeValues()
        .chunked(audio.chunkSize)
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
//            log.info("Analyzed ${it.size} samples in ${(System.nanoTime() - start).nanoseconds.inWholeMicroseconds}mc")
        }

    val titleGain = gainAnalysis.GetTitleGain(replayGain)
    log.info("Replay Gain: $titleGain (${titleGain.amplitudeToDBFS(audio.maxAmplitude)}dB) in ${millisElapsedUntilNow(start)}ms")
}

private fun readAudioWithMyImplementationOfReplayGain(
    audio: Audio,
    start: Long,
) {
    val processingResult = calculatePerceivedVolume(audio)

//    printDetailedSummary(sortedChunkSamples, rmsValue, start, fileName)
    processingResult.printSimpleSummary(start)
}

private fun printDetailedSummary(
    processingResult: ProcessingResult,
    start: Long,
) {
    val audio = processingResult.audio
    val fileName = audio.file.name
    val chunkSamples = processingResult.samples
    val rmsValue = processingResult.rmsAverageLoudness
    val maxAmplitude = audio.maxAmplitude

    log.info("""
        '$fileName' Total Samples: ${chunkSamples.size} (in ${millisElapsedUntilNow(start)}ms)
    
        Min: ${chunkSamples.first()} (${chunkSamples.first().squaredAmplitudeToDBFS(maxAmplitude)}dB)
        Max: ${chunkSamples.last()} (${chunkSamples.last().squaredAmplitudeToDBFS(maxAmplitude)}dB)
    
        Median: ${chunkSamples[chunkSamples.size / 2]} (${chunkSamples[chunkSamples.size / 2].squaredAmplitudeToDBFS(maxAmplitude)}dB)
        Mean: ${chunkSamples.average()} (${chunkSamples.average().squaredAmplitudeToDBFS(maxAmplitude)}dB)
        RMS: ${chunkSamples.rootMeanSquared()} (${chunkSamples.rootMeanSquared().amplitudeToDBFS(maxAmplitude)}dB)
    
        ReplayGain: $rmsValue (${rmsValue.squaredAmplitudeToDBFS(maxAmplitude).formattedDecimal()}dB)
    """.trimIndent())
}

private fun ProcessingResult.printSimpleSummary(start: Long) {
    val fileName = audio.file.name
    val scaledDb = rmsAverageLoudnessDB.scaleDb(audio.maxAmplitude, maxDb = 100.0)
    val mp3gainLikeDb = rmsAverageLoudnessDBFS + 113.0
//    println(millisElapsedUntilNow(start))
    log.info("$fileName -> Volume: $rmsAverageLoudness (${rmsAverageLoudnessDBFS.formattedDecimal()}dBFS, ${rmsAverageLoudnessDB.formattedDecimal()}dB, ${scaledDb.formattedDecimal()}dB (scaled), ${mp3gainLikeDb.formattedDecimal()}dB \"mp3gain-like\") in ${millisElapsedUntilNow(start)}ms")
}

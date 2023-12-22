package com.github.secretx33.mp3volume.model

import com.github.secretx33.mp3volume.amplitudeToDBFS
import com.github.secretx33.mp3volume.dBFSToDb
import com.github.secretx33.mp3volume.mp3.Audio

data class ProcessingResult(
    val audio: Audio,
    val rmsAverageLoudness: Double,
    val samples: List<Double>,
    val rmsAverageLoudnessChunkIndex: Int,
) {
    val rmsAverageLoudnessDBFS = rmsAverageLoudness.amplitudeToDBFS(audio.maxAmplitude)
    val rmsAverageLoudnessDB = rmsAverageLoudnessDBFS.dBFSToDb(audio.maxAmplitude)
}
package com.github.secretx33.mp3volume.model

import com.github.secretx33.mp3volume.mp3.Audio

data class ProcessingResult(
    val analysedAudio: Audio,
    val rmsAverageLoudness: Double,
    val samples: List<Double>,
    val rmsAverageLoudnessChunkIndex: Int,
)
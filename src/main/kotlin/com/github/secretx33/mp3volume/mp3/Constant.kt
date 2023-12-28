package com.github.secretx33.mp3volume.mp3

import kotlin.time.Duration.Companion.milliseconds

/**
 * Source: Replay Gain' [RMS Energy](https://replaygain.hydrogenaud.io/rms_energy.html).
 */
val SAMPLE_CHUNK_LENGTH = 50.milliseconds

/**
 * Source: Replay Gain' [Statistical Processing](https://replaygain.hydrogenaud.io/statistical_process.html).
 */
const val RMS_PERCENTILE = 0.95

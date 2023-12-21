package com.github.secretx33.mp3volume.model

class ProcessedSample(
    val sample: Sample,
    val processedSample: Sample,
)

typealias Sample = DoubleArray
typealias Samples = List<SampleUnit>
typealias SampleUnit = Double

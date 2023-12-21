@file:Suppress("FunctionName")

package com.github.secretx33.mp3volume.model

class ProcessedSample(
    val sample: Sample,
    val processedSample: Sample,
)

/**
 * There is no measurable loss of precision when using `Float` instead of `Double` (tested on 2023-12-21).
 */
typealias Sample = FloatArray
typealias Samples = List<SampleUnit>
typealias SampleUnit = Float

/**
 * Reunited all functions that create a Sample in one place, so that it's easier to change the type in
 * the future.
 */
fun Collection<SampleUnit>.toSampleArray(): Sample = toFloatArray()
fun sampleArrayOf(vararg elements: SampleUnit): Sample = floatArrayOf(*elements)
fun SampleArray(size: Int): Sample = FloatArray(size)
@file:Name("GainAnalysisKt")

package com.github.secretx33.mp3volume.mp3

import com.github.secretx33.mp3volume.readResource
import jdk.jfr.Name
import java.util.TreeMap
import kotlin.math.min

private val yulewalkCoeffs = readResource<TreeMap<Int, FilterCoefficients>>("coefficients/yulewalk.json")

private val butterworthCoeffs = readResource<TreeMap<Int, FilterCoefficients>>("coefficients/butterworth.json")

/**
 * Transforms the audio [samples] by approximating their values to those perceived by the
 * human ear using Yulewalk and Butterworth IIR filters.
 */
fun applyLoudnessNormalizeFilters(samples: DoubleArray, sampleRate: Int): DoubleArray {
    val yulewalkCoeffs = yulewalkCoeffs.getClosest(sampleRate)
    val butterworthCoeffs = butterworthCoeffs.getClosest(sampleRate)

    // Apply Yulewalk and Butterworth filters
    val filteredYulewalk = applyIIRFilter(samples, yulewalkCoeffs)
    val filteredButterworth = applyIIRFilter(filteredYulewalk, butterworthCoeffs)

    return filteredButterworth
}

/**
 * Apply an IIR (Infinite Impulse Response) filter, returning a copy of the `input` array.
 */
private fun applyIIRFilter(input: DoubleArray, coeffs: FilterCoefficients): DoubleArray {
    val output = DoubleArray(input.size)
    val bufferSize = coeffs.size
    val a = coeffs.a
    val b = coeffs.b

    input.indices.forEach { n ->
        var sumA = 0.0
        for (i in 1 until min(bufferSize, n)) {
            sumA += a[i] * output[n - i]
        }

        var sumB = 0.0
        for (i in 0 until min(bufferSize, n + 1)) {
            sumB += b[i] * input[n - i]
        }

        val result = sumB - sumA
        require(result.isFinite()) { "Somehow, the applying IIR filter has return a bogus value ($result), there's something very wrong in this code or the input" }

        output[n] = result
    }

    return output
}

private data class FilterCoefficients(val a: List<Double>, val b: List<Double>) {
    init {
        require(a.size == b.size) { "Filter coefficients must have the same size, a = ${a.size}, b = ${b.size}" }
    }
    val size = a.size
}

/**
 * Retrieves a value from the [TreeMap] that is closest to the given [key].
 *
 * @throws NoSuchElementException if the map is empty.
 */
@Suppress("UNCHECKED_CAST")
private fun <K : Comparable<K>, V> TreeMap<K, V>.getClosest(key: K): V {
    val closestKey = setOfNotNull(floorKey(key), ceilingKey(key)).minBy(key::compareTo)
    return get(closestKey) as V
}
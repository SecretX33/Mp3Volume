package com.github.secretx33.mp3volume

import kotlin.test.Test

class Tests {

    @Test
    fun test() {
        val range = (5 downTo 0)
        val range2 = (3 downTo 0)
        val range3 = (6 downTo 5)
        println()
    }

    fun IntProgression.isIntersect(other: IntProgression): Boolean = first <= other.last && other.first <= last

    fun IntProgression.containsAll(other: IntProgression): Boolean = first <= other.first && last >= other.last

}
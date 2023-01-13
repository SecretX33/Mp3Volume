package com.github.secretx33.kotlinplayground

import org.junit.jupiter.api.RepeatedTest
import kotlin.random.asKotlinRandom

class LevenshteinDistanceTest {

    private companion object {
        val CHARS = ('a'..'z').toList() + ' '
    }

    @RepeatedTest(20)
    fun testLevenshteinDistance() =
        repeat(15) { patternSize ->
            repeat(15) { typedWordSize ->
                repeat(1000) {
                    val pattern = generateString(patternSize)
                    val typedWord = generateString(typedWordSize)

//                    val original = DamerauLevenshtein.calculateDistance(pattern, typedWord)
//                    val ours = DamerauLevenshteinKt.calculateDistance(pattern, typedWord)
//
//                    assertEquals(original, ours)
                }
            }
        }

    private fun generateString(size: Int): String {
        val random = random.asKotlinRandom()
        val sb = StringBuilder(size)
        repeat(size) {
            sb.append(CHARS.random(random))
        }
        return sb.toString()
    }

}
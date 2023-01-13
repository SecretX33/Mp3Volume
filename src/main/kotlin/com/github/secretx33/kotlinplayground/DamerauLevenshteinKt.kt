package com.github.secretx33.kotlinplayground

object DamerauLevenshteinKt {

    /**
     * Calculates the string distance between source and target strings using
     * the Damerau-Levenshtein algorithm. The distance is case-sensitive.
     *
     * @param source The source String.
     * @param target The target String.
     * @return The distance between source and target strings.
     * @throws IllegalArgumentException If either source or target is null.
     */
    fun calculateDistance(source: CharSequence, target: CharSequence): Int {
        val sourceLength = source.length
        val targetLength = target.length
        if (sourceLength == 0) return targetLength
        if (targetLength == 0) return sourceLength

        val dist = Array(sourceLength + 1) { a ->
            when {
                a == 0 -> IntArray(targetLength + 1) { it }
                else -> IntArray(targetLength + 1) { b -> if (b == 0) a else 0 }
            }
        }
        for (i in 1..sourceLength) {
            for (j in 1..targetLength) {
                val cost = if (source[i - 1] == target[j - 1]) 0 else 1
                dist[i][j] = minOf(
                    dist[i - 1][j] + 1,
                    dist[i][j - 1] + 1,
                    dist[i - 1][j - 1] + cost,
                )
                if (i > 1 && j > 1 && source[i - 1] == target[j - 2] && source[i - 2] == target[j - 1]) {
                    dist[i][j] = minOf(dist[i][j], (dist[i - 2][j - 2] + cost))
                }
            }
        }
        return dist[sourceLength][targetLength]
    }
}

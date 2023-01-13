package com.github.secretx33.kotlinplayground.ppo

// Merge sort function
fun mergeSortAndCount(
    arr: IntArray,
    l: Int,
    r: Int,
    level: Int = 0,
    countsPerLevel: MutableMap<Int, Int> = mutableMapOf(),
): Int {

    // Keeps track of the inversion count at a
    // particular node of the recursion tree
    var count = 0
    if (l < r) {
        val m = (l + r) / 2

        // Total inversion count = left subarray count
        // + right subarray count + merge count

        // Left subarray count
        count += mergeSortAndCount(arr, l, m, level + 1, countsPerLevel)

        // Right subarray count
        count += mergeSortAndCount(arr, m + 1, r, level + 1, countsPerLevel)

        // Merge count
        count += mergeAndCount(arr, l, m, r)

        countsPerLevel[level] = countsPerLevel.getOrDefault(level, 0) + count
    }
    return count
}

// Function to count the number of inversions
// during the merge process
private fun mergeAndCount(
    arr: IntArray,
    l: Int,
    m: Int,
    r: Int,
): Int {

    // Left subarray
    val left = arr.copyOfRange(l, m + 1)

    // Right subarray
    val right = arr.copyOfRange(m + 1, r + 1)
    var i = 0
    var j = 0
    var k = l
    var swaps = 0
    while (i < left.size && j < right.size) {
        if (left[i] <= right[j]) arr[k++] = left[i++] else {
            arr[k++] = right[j++]
            swaps += m + 1 - (l + i)
        }
    }
    while (i < left.size) arr[k++] = left[i++]
    while (j < right.size) arr[k++] = right[j++]
    return swaps
}


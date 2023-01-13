//package com.github.secretx33.kotlinplayground
//
//import org.junit.jupiter.api.RepeatedTest
//import org.junit.jupiter.api.Test
//import kotlin.test.assertTrue
//
//class MatchAlgorithmTest {
//
//    private companion object {
//        const val BAG_MAX_SIZE = 12
//        const val SOURCE_MAX_SIZE = 15
//        const val INNER_SOURCE_MAX_SIZE = 10
//        val CHOICES_RANGE = 0 until 25
//    }
//
//    @Test
//    fun alwaysFailIterative() {
//        val bag = setOf(4, 8)
//        val source = listOf(
//            WeightedSet(Requirement.REQUIRED, setOf(3, 4, 7, 8, 9, 14, 19)),
//            WeightedSet(Requirement.OPTIONAL, setOf(3, 4, 11, 13, 16, 20, 25, 26)),
//            WeightedSet(Requirement.OPTIONAL, setOf(3, 7, 11, 23)),
//            WeightedSet(Requirement.REQUIRED, setOf(1, 11, 16, 17, 21, 23, 25)),
//            WeightedSet(Requirement.REQUIRED, setOf(10, 14, 15, 18, 19, 26)),
//            WeightedSet(Requirement.REQUIRED, setOf(5, 12, 13, 18, 26)),
//            WeightedSet(Requirement.REQUIRED, setOf(17, 26)),
//        )
//
//        testMatchAlgorithm(bag, source, ::maxTotalWeightRecursive)
//    }
//
//    @RepeatedTest(20)
//    fun testMaxTotalWeightRecursiveMemoized() = repeatedTestMatchAlgorithm(::maxTotalWeightRecursive)
//
////    @RepeatedTest(20)
////    fun testMaxTotalWeightIterative() = repeatedTestMatchAlgorithm(::maxTotalWeightIterative)
//
//    private fun repeatedTestMatchAlgorithm(block: (IntSet, List<WeightedSet>) -> CalculationResult) =
//        repeat(BAG_MAX_SIZE) { bagSize ->
//            repeat(SOURCE_MAX_SIZE) { sourceSize ->
//                val bag = generateBag(bagSize, CHOICES_RANGE)
//                val source = generateSource(sourceSize, CHOICES_RANGE)
//                testMatchAlgorithm(bag, source, block)
//            }
//        }
//
//    private fun testMatchAlgorithm(
//        bag: IntSet,
//        source: List<WeightedSet>,
//        block: (IntSet, List<WeightedSet>) -> CalculationResult,
//    ) {
//        val expected = bruteForceMaxTotalWeight(bag, source)
//        val actual = block(bag, source)
//        val isEquivalent = expected.isEquivalent(actual)
//
//        assertTrue(isEquivalent, "Failed for bag size '${bag.size}', source size='${source.size}'.\nBag: ${bag.sorted()}\nSource: ${source.formatToDebug()}\nExpected: ${expected.formatToDebug()}\nActual: ${actual.formatToDebug()}")
//    }
//
//    private fun generateBag(size: Int, choicesRange: IntRange): IntSet {
//        val bag = LinkedHashSet<Int>(size)
//        repeat(size) {
//            bag.add(random.nextInt(choicesRange.first, choicesRange.last))
//        }
//        return bag
//    }
//
//    private fun generateSource(size: Int, choicesRange: IntRange): List<WeightedSet> {
//        val source = List(size) {
//            val requirement = if (random.nextBoolean()) Requirement.REQUIRED else Requirement.OPTIONAL
//            val options = mutableSetOf<Int>()
//            repeat(random.nextInt(INNER_SOURCE_MAX_SIZE + 1)) {
//                options.add(random.nextInt(choicesRange.first, choicesRange.last))
//            }
//            WeightedSet(requirement, options)
//        }
//        return source
//    }
//
//    private fun CalculationResult.isEquivalent(other: CalculationResult): Boolean =
//        requiredMatched.size == other.requiredMatched.size
//            && optionalMatched.size == other.optionalMatched.size
//
//    private fun CalculationResult.formatToDebug(): CalculationResult = copy(
//        requiredMatched = requiredMatched.toSortedSet(),
//        optionalMatched = optionalMatched.toSortedSet(),
//    )
//
//    private fun List<WeightedSet>.formatToDebug(): String = filter { it.options.isNotEmpty() }
//        .map { it.copy(options = it.options.toSortedSet()) }
//        .joinToString("\n        ") { "${it.requirement} -> ${it.options}" }
//
//    private fun bruteForceMaxTotalWeight(bag: IntSet, source: List<WeightedSet>): CalculationResult {
//        if (bag.isEmpty() || source.isEmpty()) {
//            return CalculationResult.ZERO
//        }
//
//        var maxWeight = CalculationResult.ZERO
//
//        source.forEachIndexed { index, weightedSet ->
//            val options = weightedSet.options
//            val matchedItems = options.intersect(bag)
//            matchedItems.forEach { item ->
//                val result = item.toCalculationResult(weightedSet.requirement)
//                val remainingBag = bag - item
//                maxWeight = maxOf(maxWeight, result + bruteForceMaxTotalWeight(remainingBag, source.drop(index + 1)))
//            }
//        }
//
//        return maxWeight
//    }
//
//}
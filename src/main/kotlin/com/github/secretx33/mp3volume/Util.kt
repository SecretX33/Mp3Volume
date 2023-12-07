package com.github.secretx33.mp3volume

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.module.SimpleModule
import io.github.secretx33.resourceresolver.PathMatchingResourcePatternResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import org.github.jamm.MemoryMeter
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.Locale
import java.util.Random
import java.util.concurrent.ThreadLocalRandom
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists

val jackson: ObjectMapper by lazy {
    ObjectMapper().findAndRegisterModules()
        .applyProjectDefaults()
}

val prettyJackson: ObjectWriter by lazy { jackson.writerWithDefaultPrettyPrinter() }

fun ObjectMapper.applyProjectDefaults(): ObjectMapper = apply {
    registerModule(SimpleModule().apply {
        addAbstractTypeMapping(Set::class.java, LinkedHashSet::class.java)
    })
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
}

private val resourceLoader by lazy { PathMatchingResourcePatternResolver() }

fun getResourceAsString(name: String): String {
    val resource = listOf(name, "classpath:$name", "file:$name")
        .map(resourceLoader::getResource)
        .firstOrNull { it.exists() }
        ?: throw IllegalArgumentException("Resource named '$name' was not found")
    return resource.inputStream.bufferedReader().use { it.readText() }
}

fun Path.createFileIfNotExists(): Path {
    if (exists()) return this
    parent?.createDirectories()
    return createFile()
}

val cpuThreadAmount: Int by lazy { Runtime.getRuntime().availableProcessors() }

val random: Random get() = ThreadLocalRandom.current()

fun <E> List<E>.replacedAt(intRange: IntRange, ruleTokens: List<E>): List<E> =
    take(intRange.first) + ruleTokens + drop(intRange.last + 1)

fun byteArrayOutputStream(block: (ByteArrayOutputStream) -> Unit): ByteArray = ByteArrayOutputStream().use {
    block(it)
    it.toByteArray()
}

fun Long.formatFileSize(): String = when {
    this == Long.MIN_VALUE || this < 0 -> "N/A"
    this < 1024L -> "$this bytes"
    this <= 0xfffccccccccccccL shr 40 -> "%.1f KB".format(Locale.ROOT, toDouble() / (0x1 shl 10))
    this <= 0xfffccccccccccccL shr 30 -> "%.1f MB".format(Locale.ROOT, toDouble() / (0x1 shl 20))
    this <= 0xfffccccccccccccL shr 20 -> "%.1f GB".format(Locale.ROOT, toDouble() / (0x1 shl 30))
    this <= 0xfffccccccccccccL shr 10 -> "%.1f TB".format(Locale.ROOT, toDouble() / (0x1 shl 40))
    this <= 0xfffccccccccccccL -> "%.1f PB".format(Locale.ROOT, (this shr 10).toDouble() / (0x1 shl 40))
    else -> "%.1f EB".format(Locale.ROOT, (this shr 20).toDouble() / (0x1 shl 40))
}

val memoryMeter: MemoryMeter by lazy { MemoryMeter.builder().build() }

val MemoryMeter.strategy: String get() = this::class.java.getDeclaredField("strategy")
    .apply { isAccessible = true }
    .get(this)
    .let {
        when (val strategy = it::class.simpleName!!.removeSuffix("Strategy")) {
            "Unsafe" -> "$strategy (approx. size)"
            "Instrumentation" -> "$strategy (exact size)"
            else -> strategy
        }
    }

fun <T> Flow<T>.chunked(maxChunkSize: Int): Flow<List<T>> = flow {
    require(maxChunkSize >= 1) { "Invalid max chunk size (expected >= 1, actual: $maxChunkSize)" }
    var accumulator = ArrayList<T>(maxChunkSize)
    collect { value ->
        if (accumulator.size >= maxChunkSize) {
            emit(accumulator)
            accumulator = ArrayList(maxChunkSize)
        }
        accumulator += value
    }
    if (accumulator.isNotEmpty()) emit(accumulator)
}

fun <T> Flow<T>.bufferedChunked(
    maxChunkSize: Int,
    bufferCapacity: Int = 1,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
): Flow<List<T>> = chunked(maxChunkSize)
    .buffer(capacity = bufferCapacity, onBufferOverflow = onBufferOverflow)

fun <T, K : Any> Flow<T>.groupBy(keySelector: (T) -> K): Flow<Pair<K, List<T>>> = flow {
    var lastKey: K? = null
    var values = mutableListOf<T>()
    collect { value ->
        val key = keySelector(value)
        if (lastKey != null && lastKey != key) {
            emit(lastKey!! to values)
            values = mutableListOf()
        }
        lastKey = key
        values += value
    }
    if (lastKey != null && values.isNotEmpty()) emit(lastKey!! to values)
}

/**
 * Version of [collect][Flow.collect] that runs in parallel, that is, spawns a controlled number of coroutine tasks
 * to consume the items emitted by this flow.
 *
 * The number of concurrent coroutine tasks will never exceed [parallelism]. This is achieved by using `Semaphore`
 * object to suspend the collect of new items from the `Flow` if there is already [parallelism] tasks running.
 */
@Suppress("OPT_IN_USAGE")
suspend fun <T> Flow<T>.parCollect(
    parallelism: Int = DEFAULT_CONCURRENCY,
    action: suspend CoroutineScope.(value: T) -> Unit,
): Unit = coroutineScope {
    val semaphore = Semaphore(parallelism)
    collect { value ->
        semaphore.acquire()
        launch {
            try {
                action(value)
            } finally {
                semaphore.release()
            }
        }
    }
}
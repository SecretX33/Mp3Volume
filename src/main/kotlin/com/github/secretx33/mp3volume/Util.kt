package com.github.secretx33.mp3volume

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.secretx33.resourceresolver.PathMatchingResourcePatternResolver
import io.github.secretx33.resourceresolver.ResourceLoader.CLASSPATH_URL_PREFIX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.io.InputStream
import java.nio.file.Path
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Random
import java.util.concurrent.ThreadLocalRandom
import javax.sound.sampled.AudioFormat
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.reflect.typeOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

val objectMapper: ObjectMapper by lazy {
    ObjectMapper().findAndRegisterModules().applyProjectDefaults()
}

val prettyObjectMapper: ObjectWriter by lazy { objectMapper.writerWithDefaultPrettyPrinter() }

fun ObjectMapper.applyProjectDefaults(): ObjectMapper = apply {
    registerModule(SimpleModule().apply {
        addAbstractTypeMapping(Set::class.java, LinkedHashSet::class.java)
    })
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
}

private val resourceLoader by lazy { PathMatchingResourcePatternResolver() }

inline fun <reified T : Any> readResource(path: String): T {
    val resource = getResourceAsString(path)
    return try {
        objectMapper.readValue<T>(resource)
    } catch (e: Exception) {
        throw IllegalArgumentException("Failed to parse resource '$path' into type ${typeOf<T>()}", e)
    }
}

fun getResourceAsString(name: String): String {
    val resource = resourceLoader.getResource("$CLASSPATH_URL_PREFIX$name")
        .takeIf { it.isReadable }
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

val AudioFormat.frameDuration: Duration get() = (1.0 / sampleRate.toDouble()).seconds

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

fun Float.formattedDecimal(digits: Int = 1): String = toDouble().formattedDecimal(digits)

fun Double.formattedDecimal(digits: Int = 1): String {
    val pattern = "#,###${if (digits > 0) ".${"#".repeat(digits)}" else ""}"
    val format = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
    return format.format(this)
}

/**
 * Returns a string representation of the duration in seconds, with increased precision when the time is below one
 * second.
 *
 * @return a formatted string representation of the duration in seconds.
 */
fun Duration.formattedSeconds(): String {
    val secondsDouble = inWholeMilliseconds / 1000.0
    val pattern = when {
        inWholeSeconds <= 0 -> "#.##"
        else -> "#,###.#"
    }
    val format = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
    return format.format(secondsDouble)
}

/**
 * Convenience method to get a nice representation of time elapsed in micros (µ) just from the starting
 * time.
 *
 * Use [System.nanoTime] to get the starting time, then pass it to this method to get the elapsed
 * time formatted.
 */
fun microsElapsedUntilNow(startNanos: Long): String = timeElapsedUntilNow(startNanos).inWholeMicroseconds.toString()

/**
 * Convenience method to get a nice representation of time elapsed in millis just from the starting
 * time.
 *
 * Use [System.nanoTime] to get the starting time, then pass it to this method to get the elapsed
 * time formatted.
 */
fun millisElapsedUntilNow(startNanos: Long): String = timeElapsedUntilNow(startNanos).inWholeMilliseconds.toString()

/**
 * Convenience method to get a nice representation of time elapsed in seconds just from the starting time.
 *
 * Use [System.nanoTime] to get the starting time, then pass it to this method to get the elapsed time formatted.
 */
fun secondsElapsedUntilNow(startNanos: Long): String = timeElapsedUntilNow(startNanos).formattedSeconds()

fun timeElapsedUntilNow(startNanos: Long): Duration = (System.nanoTime() - startNanos).nanoseconds

fun Iterable<Float>.meanSquared(): Double = map { it.pow(2) }.average()

@JvmName("meanSquaredDouble")
fun Iterable<Double>.meanSquared(): Double = map { it.pow(2) }.average()

fun Iterable<Float>.rootMeanSquared(): Double = sqrt(meanSquared())

@JvmName("rootMeanSquaredDouble")
fun Iterable<Double>.rootMeanSquared(): Double = sqrt(meanSquared())

private const val EPSILON = 1e-10

fun Float.toDecibels(): Float = toDouble().toDecibels().toFloat()

fun Double.toDecibels(): Double = 20 * log10(this + EPSILON)

fun Float.squaredToDecibels(): Float = toDouble().toDecibels().toFloat()

fun Double.squaredToDecibels(): Double = 10 * log10(this + EPSILON)

/**
 * Read the [InputStream] as a sequence of [ByteArray]s, where each `ByteArray` has a size of [chunkSize],
 * except for the last one, which may be smaller.
 *
 * For performance reasons, this method **will reuse** the same `ByteArray` instance between reads, so the caller
 * must make a copy of the returned `ByteArray` if it needs to keep it.
 */
fun InputStream.asSequence(chunkSize: Int = DEFAULT_BUFFER_SIZE): Sequence<ByteArray> {
    var readBytes: Int
    val buffer = ByteArray(chunkSize)

    return generateSequence {
        readBytes = read(buffer)
        when {
            readBytes < 0 -> null
            readBytes < buffer.size -> buffer.copyOf(readBytes)
            else -> buffer
        }
    }
}
package com.github.secretx33.kotlinplayground

import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.HttpMethod
import java.io.IOException
import java.util.concurrent.TimeUnit

val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .readTimeout(300L, TimeUnit.SECONDS)
        .writeTimeout(300L, TimeUnit.SECONDS)
        .callTimeout(300L, TimeUnit.SECONDS)
        .build()
}

fun Request.Builder.post(): Request.Builder = method("post")

fun Request.Builder.method(method: String): Request.Builder =
    method(method.uppercase(), if (HttpMethod.requiresRequestBody(method.uppercase())) RequestBody.Companion.create(null, "") else null)

inline fun <reified T> OkHttpClient.doRequest(request: Request): T {
    val call = newCall(request)

    return call.execute().use {
        if (!it.isSuccessful) {
            throw IOException("Request to '${request.url}' failed with status code '${it.code}' and message: ${it.body?.string()}")
        }
        val body = it.body?.bytes() ?: run {
            if (null !is T) {
                throw NullPointerException("Request to ${request.url} returned a 'null' body")
            }
            return null as T
        }

        when (T::class) {
            ByteArray::class -> body as T
            String::class -> body.toString(Charsets.UTF_8) as T
            else -> jackson.readValue<T>(body)
        }
    }
}

fun <T> OkHttpClient.doBodilessRequest(request: Request, block: Response.() -> T) {
    val call = newCall(request)

    return call.execute().use {
        if (!it.isSuccessful) {
            throw IOException("Request to '${request.url}' failed with status code '${it.code}' and message: ${it.body?.string()}")
        }
        it.block()
    }
}
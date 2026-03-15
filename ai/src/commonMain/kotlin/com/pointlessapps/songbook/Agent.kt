package com.pointlessapps.songbook

import com.pointlessapps.songbook.model.OcrResponseBody
import com.pointlessapps.songbook.model.SongData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object Agent {

    private val jsonInstance = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    private const val MODEL = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonInstance)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15 * 60 * 1000L
            connectTimeoutMillis = 90 * 1000L
            socketTimeoutMillis = 15 * 60 * 1000L
        }

        engine { pipelining = false }

        expectSuccess = false

        install(DefaultRequest) {
            headers.append(HttpHeaders.Accept, "application/json")
            headers.append(HttpHeaders.AcceptEncoding, "identity")
            headers.append(HttpHeaders.Connection, "close")
            headers.append(HttpHeaders.CacheControl, "no-cache")
        }
    }

    suspend fun extractSongData(bytes: ByteArray): SongData? {
        val response = httpClient.post("$BASE_URL/$MODEL:generateContent") {
            parameter("key", "AIzaSyBM2JGn74cCsqf2aotqWmiyn55A56AigVg")
            contentType(ContentType.Application.Json)
            setBody(
                createOcrRequestBody(
                    prompt = createOcrPrompt(),
                    bytes = bytes,
                    mimeType = "image/jpeg",
                ),
            )

            timeout {
                requestTimeoutMillis = 30_000L
                connectTimeoutMillis = 30_000L
                socketTimeoutMillis = 30_000L
            }
        }

        val body = withContext(Dispatchers.Default) { response.body<OcrResponseBody>() }
        val responseText = body.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
        return responseText?.removeSurrounding(
            prefix = "```json",
            suffix = "```",
        )?.let(jsonInstance::decodeFromString)
    }
}

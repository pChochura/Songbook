package com.pointlessapps.songbook

import com.pointlessapps.songbook.model.G4fResponseBody
import com.pointlessapps.songbook.model.OcrResponseBody
import com.pointlessapps.songbook.model.OllamaResponseBody
import com.pointlessapps.songbook.model.SongData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
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
    var type: Type = Type.Gemini

    suspend fun extractSongData(bytes: ByteArray): List<SongData>? {
        val agent: AgentImplementation = when (type) {
            Type.Gemini -> GeminiAgent
            Type.Ollama -> OllamaAgent
            Type.G4f -> G4fAgent
        }
        return agent.extractSongData(bytes)
    }

    enum class Type { Gemini, Ollama, G4f }
}

private interface AgentImplementation {
    suspend fun extractSongData(bytes: ByteArray): List<SongData>?
}

private val jsonInstance = Json {
    coerceInputValues = true
    ignoreUnknownKeys = true
    isLenient = true
}

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

private object GeminiAgent : AgentImplementation {
    private const val MODEL = "gemini-2.5-flash-lite"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    override suspend fun extractSongData(bytes: ByteArray): List<SongData>? {
        val response = httpClient.post("$BASE_URL/$MODEL:generateContent") {
//            parameter("key", geminiApiKey)
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
        return runCatching<List<SongData>?> {
            responseText?.removeSurrounding(
                prefix = "```json",
                suffix = "```",
            )?.let(jsonInstance::decodeFromString)
        }.getOrNull()
    }
}

private object OllamaAgent : AgentImplementation {
    private const val BASE_URL = "https://ollama.com/api/generate"

    override suspend fun extractSongData(bytes: ByteArray): List<SongData>? {
        val response = httpClient.post(BASE_URL) {
//            headers.append(HttpHeaders.Authorization, "Bearer $ollamaApiKey")
            contentType(ContentType.Application.Json)
            setBody(
                createOllamaRequestBody(
                    prompt = createOcrPrompt(),
                    bytes = bytes,
                ),
            )

            timeout {
                requestTimeoutMillis = 30_000L
                connectTimeoutMillis = 30_000L
                socketTimeoutMillis = 30_000L
            }
        }

        val body = withContext(Dispatchers.Default) { response.body<OllamaResponseBody>() }
        return runCatching<List<SongData>?> {
            body.response.removeSurrounding(
                prefix = "```json",
                suffix = "```",
            ).let(jsonInstance::decodeFromString)
        }.getOrNull()
    }
}

private object G4fAgent : AgentImplementation {
    private const val BASE_URL = "https://g4f.dev/v1/chat/completions"

    override suspend fun extractSongData(bytes: ByteArray): List<SongData>? {
        val response = httpClient.post(BASE_URL) {
//            headers.append(HttpHeaders.Authorization, "Bearer $g4fApiKey")
            contentType(ContentType.Application.Json)
            setBody(
                createG4fRequestBody(
                    prompt = createOcrPrompt(),
                    bytes = bytes,
                ),
            )

            timeout {
                requestTimeoutMillis = 30_000L
                connectTimeoutMillis = 30_000L
                socketTimeoutMillis = 30_000L
            }
        }

        val body = withContext(Dispatchers.Default) { response.body<G4fResponseBody>() }
        val responseText = body.choices.firstOrNull()?.message?.content
        return runCatching<List<SongData>?> {
            responseText?.removeSurrounding(
                prefix = "```json",
                suffix = "```",
            )?.let(jsonInstance::decodeFromString)
        }.getOrNull()
    }
}

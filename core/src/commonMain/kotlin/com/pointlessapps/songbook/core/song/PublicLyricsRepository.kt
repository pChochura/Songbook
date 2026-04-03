package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.PublicLyrics
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface PublicLyricsRepository {
    fun searchPublicLyrics(query: String): Flow<List<PublicLyrics>>
}

internal class PublicLyricsRepositoryImpl(
    private val httpClient: HttpClient,
) : PublicLyricsRepository {

    override fun searchPublicLyrics(query: String): Flow<List<PublicLyrics>> = flow {
        runCatching {
            val response = httpClient.get(PUBLIC_LYRICS_URL) {
                parameter("q", query)
                header("User-Agent", USER_AGENT)
            }
            val results: List<PublicLyrics> = withContext(Dispatchers.Default) { response.body() }
            emit(results)
        }.getOrElse { emit(emptyList()) }
    }.flowOn(Dispatchers.IO)

    private companion object {
        const val PUBLIC_LYRICS_URL = "https://lrclib.net/api/search"
        const val USER_AGENT = "Songbook v1.0.0 (https://github.com/pChochura/Songbook)"
    }
}

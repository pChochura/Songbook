package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.PublicLyrics
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
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
            val response = httpClient.get("https://lrclib.net/api/search") {
                parameter("q", query)
            }
            val results: List<PublicLyrics> = withContext(Dispatchers.Default) { response.body() }
            emit(results)
        }.getOrElse { emit(emptyList()) }
    }.flowOn(Dispatchers.IO)
}

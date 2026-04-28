package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.PublicLyrics
import com.pointlessapps.songbook.core.utils.emptyImmutableList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

internal class PublicLyricsRepositoryImpl(
    private val httpClient: HttpClient,
) : PublicLyricsRepository {

    override fun searchPublicLyrics(query: String): Flow<ImmutableList<PublicLyrics>> = flow {
        runCatching {
            val response = httpClient.get(PUBLIC_LYRICS_URL) {
                parameter("q", query)
                header("User-Agent", USER_AGENT)
            }
            val results: List<PublicLyrics> = withContext(Dispatchers.Default) { response.body() }
            emit(results.toImmutableList())
        }.getOrElse { emit(emptyImmutableList()) }
    }.flowOn(Dispatchers.IO)

    private companion object {
        const val PUBLIC_LYRICS_URL = "https://lrclib.net/api/search"
        const val USER_AGENT = "Songbook v1.0.0 (https://github.com/pChochura/Songbook)"
    }
}

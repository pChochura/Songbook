package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.PublicLyrics
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class WasmPublicLyricsRepositoryImpl : PublicLyricsRepository {
    override fun searchPublicLyrics(query: String): Flow<ImmutableList<PublicLyrics>> = emptyFlow()
}

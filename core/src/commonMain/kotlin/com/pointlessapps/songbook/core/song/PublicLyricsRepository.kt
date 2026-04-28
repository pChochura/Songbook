package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.PublicLyrics
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface PublicLyricsRepository {
    fun searchPublicLyrics(query: String): Flow<ImmutableList<PublicLyrics>>
}

package com.pointlessapps.songbook.core.song.model

import androidx.compose.runtime.Stable

@Stable
data class SongSearchResult(
    val id: Int,
    val songId: String,
    val title: String,
    val artist: String,
    val snippet: String,
)

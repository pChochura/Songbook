package com.pointlessapps.songbook.core.song.database.entity

data class SongSearchResult(
    val id: Int,
    val songId: String,
    val title: String,
    val artist: String,
    val snippet: String,
)

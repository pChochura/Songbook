package com.pointlessapps.songbook.core.song.database.entity

data class SongSearchResult(
    val id: Long,
    val title: String,
    val artist: String,
    val snippet: String,
)

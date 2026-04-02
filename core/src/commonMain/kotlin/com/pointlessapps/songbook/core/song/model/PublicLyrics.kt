package com.pointlessapps.songbook.core.song.model

import kotlinx.serialization.Serializable

@Serializable
data class PublicLyrics(
    val id: Long,
    val artistName: String,
    val trackName: String,
    val plainLyrics: String,
)

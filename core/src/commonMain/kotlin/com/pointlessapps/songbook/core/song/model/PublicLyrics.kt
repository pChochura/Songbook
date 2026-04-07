package com.pointlessapps.songbook.core.song.model

import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class PublicLyrics(
    val id: Long,
    val artistName: String,
    val trackName: String,
    val plainLyrics: String,
)

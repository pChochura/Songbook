package com.pointlessapps.songbook.core.song.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Stable
@Immutable
data class PublicLyrics(
    val id: Long,
    val artistName: String,
    val trackName: String,
    val plainLyrics: String,
)

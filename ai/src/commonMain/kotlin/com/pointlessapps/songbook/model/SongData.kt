package com.pointlessapps.songbook.model

import com.pointlessapps.songbook.utils.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class SongData(
    @SerialName("title")
    val title: String?,
    @SerialName("artist")
    val author: String?,
    @SerialName("chords_beside")
    val chordsBeside: List<String>,
    @SerialName("content")
    val content: String,
)

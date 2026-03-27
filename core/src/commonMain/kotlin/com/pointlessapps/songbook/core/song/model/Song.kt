package com.pointlessapps.songbook.core.song.model

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val sections: List<Section>,
)

@Serializable
data class Section(
    val name: String,
    val lyrics: String,
    val chords: List<String>,
)

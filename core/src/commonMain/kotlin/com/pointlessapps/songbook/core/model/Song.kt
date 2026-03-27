package com.pointlessapps.songbook.core.model

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
    val text: String,
    val chords: List<Chord>,
)

@Serializable
data class Chord(val value: String)

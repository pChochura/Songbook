package com.pointlessapps.songbook.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ParsedLine(
    val text: String,
    val chords: List<ChordMarker> = emptyList(),
)

package com.pointlessapps.songbook.core.song.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Stable
@Immutable
data class ChordsData(
    val keys: List<String>,
    val chords: Map<String, Map<String, List<ChordPosition>>>,
)

@Keep
@Serializable
@Stable
@Immutable
data class ChordPosition(
    val frets: List<Int>,
    val fingers: List<Int>,
    val baseFret: Int,
    val barres: List<Int>,
)

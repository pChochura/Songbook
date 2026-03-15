package com.pointlessapps.songbook.model

import kotlinx.serialization.Serializable

@Serializable
data class SongData(
    val title: String?,
    val author: String?,
    val sections: List<Section>,
) {
    @Serializable
    data class Section(
        val type: Type,
        val lines: List<Line>,
    ) {
        @Serializable
        enum class Type { Verse, Chorus, Bridge }

        @Serializable
        data class Line(
            val text: String,
            val chords: List<String>,
        )
    }
}

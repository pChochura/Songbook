package com.pointlessapps.songbook.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongData(
    @SerialName("title")
    val title: String?,
    @SerialName("artist")
    val author: String?,
    @SerialName("sections")
    val sections: List<Section>,
) {
    @Serializable
    data class Section(
        @SerialName("type")
        val type: Type,
        @SerialName("chords_beside")
        val chordsBeside: List<String>,
        @SerialName("lines")
        val lines: List<Line>,
    ) {
        @Serializable
        enum class Type {
            @SerialName("verse")
            Verse,

            @SerialName("chorus")
            Chorus,

            @SerialName("bridge")
            Bridge
        }

        @Serializable
        data class Line(
            @SerialName("text")
            val text: String,
            @SerialName("chords_above")
            val chordsAbove: List<String>,
        )
    }
}

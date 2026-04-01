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
data class NewSong(
    val title: String,
    val artist: String,
    val sections: List<Section>,
)

@Serializable
data class Section(
    val name: String,
    val lyrics: String,
    val chords: List<Chord>,
) {
    data class Line(
        val line: String,
        val chords: List<Chord>,
    )

    val lines: List<Line>
        get() {
            var currentPos = 0
            return lyrics.lines().map { lineText ->
                val lineChords = chords
                    .filter { it.position in currentPos..(currentPos + lineText.length) }
                    .map { it.copy(position = it.position - currentPos) }

                currentPos += lineText.length + 1
                Line(lineText, lineChords)
            }
        }
}

@Serializable
data class Chord(
    val value: String,
    val position: Int,
)

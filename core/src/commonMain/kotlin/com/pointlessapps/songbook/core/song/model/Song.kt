package com.pointlessapps.songbook.core.song.model

import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val sections: List<Section>,
)

@Keep
@Serializable
data class NewSong(
    val id: Long? = null,
    val title: String,
    val artist: String,
    val sections: List<Section>,
)

@Keep
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

    companion object {
        fun List<Section>.toLyrics(withChords: Boolean = true): String =
            joinToString("\n\n") { section ->
                val lines = section.lines.joinToString("\n") { line ->
                    val builder = StringBuilder(line.line)
                    if (withChords) {
                        line.chords.sortedByDescending { it.position }.forEach { chord ->
                            builder.insert(chord.position, "[${chord.value}]")
                        }
                    }
                    builder.toString()
                }
                "[${section.name}]\n$lines"
            }
    }
}

@Keep
@Serializable
data class Chord(
    val value: String,
    val position: Int,
)

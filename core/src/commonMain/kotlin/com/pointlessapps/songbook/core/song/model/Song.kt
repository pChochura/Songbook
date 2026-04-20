package com.pointlessapps.songbook.core.song.model

import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Keep
@Serializable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val lyrics: String,
)

@Keep
@Serializable
data class NewSong(
    val id: String? = null,
    val title: String,
    val artist: String,
    val lyrics: String,
)

@Keep
@Serializable
@OptIn(ExperimentalUuidApi::class)
data class Section(
    val id: Int,
    val name: String,
    val lyrics: String,
    val chords: List<Chord>,
) {
    @Keep
    @Serializable
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

                currentPos += lineText.length + 1
                Line(lineText, lineChords)
            }
        }

    companion object {
        fun List<Section>.toLyrics(
            withChords: Boolean = true,
        ): String = joinToString("\n\n") { section ->
            val builder = StringBuilder(section.lyrics)
            if (withChords) {
                section.chords.sortedByDescending(Chord::position).forEach { chord ->
                    builder.insert(chord.position, "[${chord.value}]")
                }
            }

            if (section.name.isNotEmpty()) {
                "[${section.name}]\n$builder"
            } else {
                builder.toString()
            }
        }
    }
}

@Keep
@Serializable
data class Chord(
    val value: String,
    val position: Int,
    val linePosition: Int,
)

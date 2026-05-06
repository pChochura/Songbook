package com.pointlessapps.songbook.core.song.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Keep
@Serializable
@Stable
@Immutable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val lyrics: String,
) {
    enum class SortBy { Title, Artist, DateAdded }
}

@Keep
@Serializable
@Stable
@Immutable
data class NewSong(
    val id: String? = null,
    val title: String,
    val artist: String,
    val lyrics: String,
)

@Keep
@Serializable
@OptIn(ExperimentalUuidApi::class)
@Stable
@Immutable
data class Section(
    val id: Int,
    val name: String,
    val lyrics: String,
    val chords: ImmutableList<Chord>,
) {
    @Keep
    @Serializable
    @Stable
    @Immutable
    data class Line(
        val line: String,
        val chords: ImmutableList<Chord>,
    )

    val lines: ImmutableList<Line>
        get() {
            var currentPos = 0
            return lyrics.lines().map { lineText ->
                val lineChords = chords
                    .filter { it.position in currentPos..(currentPos + lineText.length) }

                currentPos += lineText.length + 1
                Line(lineText, lineChords.toImmutableList())
            }.toImmutableList()
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
@Stable
@Immutable
data class Chord(
    val value: String,
    val position: Int,
    val linePosition: Int,
)

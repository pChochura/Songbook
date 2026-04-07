package com.pointlessapps.songbook.model

import com.pointlessapps.songbook.utils.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class SongData(
    @SerialName("title")
    val title: String?,
    @SerialName("artist")
    val author: String?,
    @SerialName("sections")
    val sections: List<Section>,
) {
    @Keep
    @Serializable
    data class Section(
        @SerialName("type")
        val type: Type,
        @SerialName("chords_beside")
        val chordsBeside: List<String>,
        @SerialName("lines")
        val lines: List<Line>,
    ) {
        @Keep
        @Serializable
        enum class Type {
            @SerialName("verse")
            Verse,

            @SerialName("chorus")
            Chorus,

            @SerialName("bridge")
            Bridge,

            @SerialName("outro")
            Outro,

            @SerialName("intro")
            Intro
        }

        @Keep
        @Serializable
        data class Line(
            @SerialName("text")
            val text: String,
            @SerialName("chords_above")
            val chordsAbove: List<String>,
        ) {
            fun toLyrics(): String {
                if (chordsAbove.isEmpty()) return text
                val words = text.split(" ")
                if (words.isEmpty()) return chordsAbove.joinToString("") { "[$it]" }

                val result = words.toMutableList()
                chordsAbove.forEachIndexed { index, chord ->
                    if (chord.isNotBlank()) {
                        val wordIndex =
                            (index * words.size / chordsAbove.size).coerceAtMost(words.size - 1)
                        result[wordIndex] = "[$chord]${result[wordIndex]}"
                    }
                }
                return result.joinToString(" ")
            }
        }

        fun toLyrics(index: Int): String = buildString {
            append("[$type $index]\n")
            append(lines.joinToString("\n") { it.toLyrics() })
            if (chordsBeside.isNotEmpty()) {
                append("\n")
                append(chordsBeside.joinToString(" ") { "[$it]" })
            }
        }
    }

    fun toLyrics(): String {
        val sectionTypeCount = mutableMapOf<Section.Type, Int>()
        return sections.joinToString("\n\n") { section ->
            val count = sectionTypeCount.getOrPut(section.type) { 0 } + 1
            sectionTypeCount[section.type] = count
            section.toLyrics(count)
        }
    }
}

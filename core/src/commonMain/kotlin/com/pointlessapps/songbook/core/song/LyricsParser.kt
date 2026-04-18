package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.Chord
import com.pointlessapps.songbook.core.song.model.Section

object LyricsParser {
    private val sectionHeaderRegex = Regex("""^\[([^]]+)]$""", RegexOption.IGNORE_CASE)
    private val chordRegex = Regex("""\[([^]]+)]""")

    fun parseLyrics(lyrics: String): List<Section> {
        val sections = mutableListOf<Section>()

        var currentSectionName = ""
        val currentSectionLyrics = StringBuilder()
        val currentSectionChords = mutableListOf<Chord>()

        fun emitSection() {
            if (currentSectionName.isEmpty() && currentSectionLyrics.isEmpty() && currentSectionChords.isEmpty()) {
                return
            }

            val lyricsString = currentSectionLyrics.toString().trimEnd()
            val leadingWhitespace = lyricsString.takeWhile { it.isWhitespace() }.length
            val finalLyrics = lyricsString.substring(leadingWhitespace)
            val finalChords = currentSectionChords.map {
                it.copy(position = (it.position - leadingWhitespace).coerceAtLeast(0))
            }

            sections.add(
                Section(
                    name = currentSectionName,
                    lyrics = finalLyrics,
                    chords = finalChords,
                ),
            )

            currentSectionName = ""
            currentSectionLyrics.clear()
            currentSectionChords.clear()
        }

        lyrics.lines().forEach { line ->
            if (sectionHeaderRegex.matches(line.trim())) {
                emitSection()
                currentSectionName = line.trim().removeSurrounding("[", "]")
            } else if (line.isNotBlank()) {
                var lineLyrics = line
                val lineChords = mutableListOf<Chord>()

                var match = chordRegex.find(lineLyrics)
                while (match != null) {
                    val chordValue = match.groupValues[1]
                    val chordPosInLine = match.range.first

                    lineChords.add(
                        Chord(
                            value = chordValue,
                            position = currentSectionLyrics.length + (if (currentSectionLyrics.isNotEmpty()) 1 else 0) + chordPosInLine,
                        ),
                    )

                    lineLyrics = lineLyrics.removeRange(match.range)
                    match = chordRegex.find(lineLyrics)
                }

                if (currentSectionLyrics.isNotEmpty()) {
                    currentSectionLyrics.append("\n")
                }
                currentSectionLyrics.append(lineLyrics)
                currentSectionChords.addAll(lineChords)
            }
        }

        emitSection()

        return sections
    }
}

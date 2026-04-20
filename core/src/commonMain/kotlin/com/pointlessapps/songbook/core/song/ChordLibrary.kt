package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.ChordPosition
import com.pointlessapps.songbook.core.song.model.ChordsData

class ChordLibrary {

    private lateinit var data: ChordsData

    fun initialize(data: ChordsData) {
        if (!::data.isInitialized) {
            this.data = data
        }
    }

    fun getChordPositions(chordName: String): List<ChordPosition> {
        val match = rootNoteRegex.find(chordName) ?: return emptyList()
        val root = match.groupValues[1]
        val suffix = chordName.substring(root.length)

        val mapKey = rootToMapKey[root] ?: return emptyList()
        val internalSuffix = commonSuffixToInternal[suffix] ?: suffix

        return data.chords[mapKey]?.get(internalSuffix) ?: emptyList()
    }

    companion object {
        private val rootNoteRegex = Regex("^([A-G][#b]?)")

        private val chromaticScale = listOf(
            listOf("C"), listOf("C#", "Db"), listOf("D"), listOf("D#", "Eb"),
            listOf("E"), listOf("F"), listOf("F#", "Gb"), listOf("G"),
            listOf("G#", "Ab"), listOf("A"), listOf("A#", "Bb"), listOf("B"),
        )

        private val rootToMapKey = mapOf(
            "C" to "C", "C#" to "Csharp", "Db" to "Csharp",
            "D" to "D", "D#" to "Eb", "Eb" to "Eb",
            "E" to "E", "F" to "F", "F#" to "Fsharp", "Gb" to "Fsharp",
            "G" to "G", "G#" to "Ab", "Ab" to "Ab",
            "A" to "A", "A#" to "Bb", "Bb" to "Bb",
            "B" to "B",
        )

        private val commonSuffixToInternal = mapOf(
            "" to "major", "M" to "major", "maj" to "major",
            "m" to "minor", "min" to "minor", "-" to "minor",
            "7" to "7",
            "maj7" to "maj7", "M7" to "maj7",
            "m7" to "m7", "min7" to "m7", "-7" to "m7",
            "sus2" to "sus2", "sus4" to "sus4", "sus" to "sus4",
            "dim" to "dim", "dim7" to "dim7",
            "aug" to "aug", "+" to "aug",
            "5" to "5", "6" to "6", "9" to "9",
            "add9" to "add9", "madd9" to "madd9",
        )

        val allChords: List<String> = chromaticScale.flatten().flatMap { note ->
            commonSuffixToInternal.keys.map { suffix -> "$note$suffix" }
        }.distinct().sorted()

        private fun transposeNote(note: String, offset: Int): String {
            val match = rootNoteRegex.find(note) ?: return note
            val root = match.groupValues[1]
            val suffix = note.substring(root.length)

            val index = chromaticScale.indexOfFirst { it.contains(root) }
            if (index == -1) return note

            var newIndex = (index + offset) % 12
            while (newIndex < 0) newIndex += 12

            val newRoot = chromaticScale[newIndex].first()

            return "$newRoot$suffix"
        }

        fun transpose(chord: String, offset: Int): String {
            if (offset == 0) return chord

            val parts = chord.split("/")
            val transposedRoot = transposeNote(parts[0], offset)
            val transposedBass = if (parts.size > 1) "/${transposeNote(parts[1], offset)}" else ""

            return "$transposedRoot$transposedBass"
        }
    }
}

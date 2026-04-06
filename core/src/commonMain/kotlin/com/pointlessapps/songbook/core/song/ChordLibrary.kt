package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.ChordShape

object ChordLibrary {
    enum class Shape(val chordShape: ChordShape) {
        SHAPE_MAJOR_E(ChordShape("MAJOR_E", listOf(0, 2, 2, 1, 0, 0), 0)),
        SHAPE_MINOR_E(ChordShape("MINOR_E", listOf(0, 2, 2, 0, 0, 0), 0)),
        SHAPE_DOM7_E(ChordShape("DOM7_E", listOf(0, 2, 0, 1, 0, 0), 0)),
        SHAPE_POWER_E(ChordShape("POWER_E", listOf(0, 2, 2, null, null, null), 0)),
        SHAPE_DIM_A(ChordShape("DIM_A", listOf(null, 0, 1, 2, 1, null), 1)),
        SHAPE_DIM7_A(ChordShape("DIM7_A", listOf(null, 0, 1, 0, 1, null), 1)),
        SHAPE_AUG_A(ChordShape("AUG_A", listOf(null, 0, 2, 1, 1, null), 1)),
        SHAPE_SUS2_A(ChordShape("SUS2_A", listOf(null, 0, 2, 2, 3, null), 1)),
        SHAPE_SUS4_E(ChordShape("SUS4_E", listOf(0, 2, 2, 2, 0, 0), 0)),
        SHAPE_MAJ7_A(ChordShape("MAJ7_A", listOf(null, 0, 2, 1, 2, 0), 1)),
        SHAPE_MIN7_E(ChordShape("MIN7_E", listOf(0, 2, 0, 0, 0, 0), 0)),
        SHAPE_7SUS4_E(ChordShape("7SUS4_E", listOf(0, 2, 0, 2, 0, 0), 0)),
    }

    private val qualityToShape = mapOf(
        "" to Shape.SHAPE_MAJOR_E,
        "M" to Shape.SHAPE_MAJOR_E,
        "maj" to Shape.SHAPE_MAJOR_E,

        "m" to Shape.SHAPE_MINOR_E,
        "min" to Shape.SHAPE_MINOR_E,

        "7" to Shape.SHAPE_DOM7_E,

        "5" to Shape.SHAPE_POWER_E,

        "dim" to Shape.SHAPE_DIM_A,
        "°" to Shape.SHAPE_DIM_A,

        "dim7" to Shape.SHAPE_DIM7_A,
        "o7" to Shape.SHAPE_DIM7_A,
        "7dim" to Shape.SHAPE_DIM7_A,

        "aug" to Shape.SHAPE_AUG_A,
        "+" to Shape.SHAPE_AUG_A,

        "sus2" to Shape.SHAPE_SUS2_A,

        "sus" to Shape.SHAPE_SUS4_E,
        "sus4" to Shape.SHAPE_SUS4_E,

        "maj7" to Shape.SHAPE_MAJ7_A,
        "M7" to Shape.SHAPE_MAJ7_A,

        "m7" to Shape.SHAPE_MIN7_E,
        "min7" to Shape.SHAPE_MIN7_E,
        "-7" to Shape.SHAPE_MIN7_E,

        "7sus4" to Shape.SHAPE_7SUS4_E,
        "7sus" to Shape.SHAPE_7SUS4_E,
    )

    private val chromaticScale = listOf(
        listOf("C"), listOf("C#", "Db"), listOf("D"), listOf("D#", "Eb"),
        listOf("E"), listOf("F"), listOf("F#", "Gb"), listOf("G"),
        listOf("G#", "Ab"), listOf("A"), listOf("A#", "Bb"), listOf("B"),
    )

    val allChords = chromaticScale.flatten().flatMap { note ->
        qualityToShape.keys.map { quality -> "$note$quality" }
    }.distinct()

    // Starting notes for each string (Standard Tuning: E A D G B E)
    private val stringRoots = listOf(4, 9, 2, 7, 11, 4)

    private val rootNoteRegex = Regex("^([A-G][#b]?)")

    fun transpose(chord: String, offset: Int): String {
        if (offset == 0) return chord

        val parts = chord.split("/")
        val transposedRoot = transposeNote(parts[0], offset)
        val transposedBass = if (parts.size > 1) "/${transposeNote(parts[1], offset)}" else ""

        return "$transposedRoot$transposedBass"
    }

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

    fun getBaseFret(note: String, rootString: Int): Int {
        val targetIdx = chromaticScale.indexOfFirst { it.contains(note) }
        val stringIdx = stringRoots[rootString]

        var fret = targetIdx - stringIdx
        while (fret < 0) fret += 12
        return fret
    }
}

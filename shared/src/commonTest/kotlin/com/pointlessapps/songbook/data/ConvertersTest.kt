package com.pointlessapps.songbook.data

import com.pointlessapps.songbook.core.domain.models.Chord
import com.pointlessapps.songbook.core.domain.models.ChordMarker
import com.pointlessapps.songbook.core.domain.models.Note
import com.pointlessapps.songbook.core.domain.models.ParsedLine
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun testSerialization() {
        val sections = listOf(
            listOf(
                ParsedLine(
                    text = "Hello World",
                    chords = listOf(
                        ChordMarker(Chord(Note.C), 0),
                        ChordMarker(Chord(Note.G), 6)
                    )
                )
            )
        )

        val serialized = converters.fromSections(sections)
        val deserialized = converters.toSections(serialized)

        assertEquals(sections, deserialized)
    }
}

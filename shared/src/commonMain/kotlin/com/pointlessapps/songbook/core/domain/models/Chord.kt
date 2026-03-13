package com.pointlessapps.songbook.core.domain.models

/**
 * Represents the 12 semitones in Western music.
 */
enum class Note(val sharp: String, val flat: String) {
    C("C", "C"), Cs("C#", "Db"), D("D", "D"), Ds("D#", "Eb"),
    E("E", "E"), F("F", "F"), Fs("F#", "Gb"), G("G", "G"),
    Gs("G#", "Ab"), A("A", "A"), As("A#", "Bb"), B("B", "B");

    fun transpose(semitones: Int): Note {
        val entries = entries
        val newIndex = (ordinal + semitones).let {
            if (it >= 0) it % 12 else (it % 12 + 12) % 12
        }
        return entries[newIndex]
    }

    companion object {
        fun fromString(value: String): Note? = entries.find {
            it.sharp.equals(value, ignoreCase = true) || it.flat.equals(value, ignoreCase = true)
        }
    }
}

data class Chord(
    val root: Note,
    val suffix: String = "",
    val useFlats: Boolean = false,
) {
    val value: String get() = (if (useFlats) root.flat else root.sharp) + suffix

    fun transpose(semitones: Int): Chord = copy(root = root.transpose(semitones))

    companion object {
        // Common chords for the selection popup
        val allCommon: List<Chord> by lazy {
            val suffixes = listOf("", "m", "7", "m7", "Maj7", "sus4", "add9", "m7b5")
            Note.entries.flatMap { note ->
                suffixes.map { Chord(note, it) }
            }
        }

        fun fromString(value: String): Chord? {
            if (value.isEmpty()) return null
            val rootStr = if (value.length > 1 && (value[1] == '#' || value[1] == 'b')) {
                value.substring(0, 2)
            } else {
                value.substring(0, 1)
            }
            val note = Note.fromString(rootStr) ?: return null
            return Chord(note, value.substring(rootStr.length))
        }

        // Helper properties to maintain backward compatibility with previous enum usage if needed
        val A = Chord(Note.A)
        val Am = Chord(Note.A, "m")
        val A7 = Chord(Note.A, "7")
        val AMaj7 = Chord(Note.A, "Maj7")
        val Am7 = Chord(Note.A, "m7")
        val Asf = Chord(Note.As, "")
        val Asm = Chord(Note.As, "m")
        val As7 = Chord(Note.As, "7")
        val AsMaj7 = Chord(Note.As, "Maj7")
        val Asm7 = Chord(Note.As, "m7")
        val B = Chord(Note.B)
        val Bm = Chord(Note.B, "m")
        val B7 = Chord(Note.B, "7")
        val BMaj7 = Chord(Note.B, "Maj7")
        val Bm7 = Chord(Note.B, "m7")
        val Bm7b5 = Chord(Note.B, "m7b5")
        val C = Chord(Note.C)
        val Cm = Chord(Note.C, "m")
        val C7 = Chord(Note.C, "7")
        val CMaj7 = Chord(Note.C, "Maj7")
        val Cm7 = Chord(Note.C, "m7")
        val Csf = Chord(Note.Cs, "")
        val Csm = Chord(Note.Cs, "m")
        val Cs7 = Chord(Note.Cs, "7")
        val CsMaj7 = Chord(Note.Cs, "Maj7")
        val Csm7 = Chord(Note.Cs, "m7")
        val D = Chord(Note.D)
        val Dm = Chord(Note.D, "m")
        val D7 = Chord(Note.D, "7")
        val DMaj7 = Chord(Note.D, "Maj7")
        val Dm7 = Chord(Note.D, "m7")
        val Dsf = Chord(Note.Ds, "")
        val Dsm = Chord(Note.Ds, "m")
        val Ds7 = Chord(Note.Ds, "7")
        val DsMaj7 = Chord(Note.Ds, "Maj7")
        val Dsm7 = Chord(Note.Ds, "m7")
        val E = Chord(Note.E)
        val Em = Chord(Note.E, "m")
        val E7 = Chord(Note.E, "7")
        val EMaj7 = Chord(Note.E, "Maj7")
        val Em7 = Chord(Note.E, "m7")
        val F = Chord(Note.F)
        val Fm = Chord(Note.F, "m")
        val F7 = Chord(Note.F, "7")
        val FMaj7 = Chord(Note.F, "Maj7")
        val Fm7 = Chord(Note.F, "m7")
        val Fsf = Chord(Note.Fs, "")
        val Fsm = Chord(Note.Fs, "m")
        val Fs7 = Chord(Note.Fs, "7")
        val FsMaj7 = Chord(Note.Fs, "Maj7")
        val Fsm7 = Chord(Note.Fs, "m7")
        val G = Chord(Note.G)
        val Gm = Chord(Note.G, "m")
        val G7 = Chord(Note.G, "7")
        val GMaj7 = Chord(Note.G, "Maj7")
        val Gm7 = Chord(Note.G, "m7")
        val Gsf = Chord(Note.Gs, "")
        val Gsm = Chord(Note.Gs, "m")
        val Gs7 = Chord(Note.Gs, "7")
        val GsMaj7 = Chord(Note.Gs, "Maj7")
        val Gsm7 = Chord(Note.Gs, "m7")
    }
}

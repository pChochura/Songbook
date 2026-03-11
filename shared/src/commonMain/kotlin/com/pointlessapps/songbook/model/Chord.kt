package com.pointlessapps.songbook.model

enum class Chord(val value: String) {
    A("A"), Am("Am"), A7("A7"), AMaj7("AMaj7"), Am7("Am7"),
    Asf("A#"), Asm("A#m"), As7("A#7"), AsMaj7("A#Maj7"), Asm7("A#m7"),
    B("B"), Bm("Bm"), B7("B7"), BMaj7("BMaj7"), Bm7("Bm7"), Bm7b5("Bm7b5"),
    C("C"), Cm("Cm"), C7("C7"), CMaj7("CMaj7"), Cm7("Cm7"),
    Csf("C#"), Csm("C#m"), Cs7("C#7"), CsMaj7("C#Maj7"), Csm7("C#m7"),
    D("D"), Dm("Dm"), D7("D7"), DMaj7("DMaj7"), Dm7("Dm7"),
    Dsf("D#"), Dsm("D#m"), Ds7("D#7"), DsMaj7("D#Maj7"), Dsm7("D#m7"),
    E("E"), Em("Em"), E7("E7"), EMaj7("EMaj7"), Em7("Em7"),
    F("F"), Fm("Fm"), F7("F7"), FMaj7("FMaj7"), Fm7("Fm7"),
    Fsf("F#"), Fsm("F#m"), Fs7("F#7"), FsMaj7("F#Maj7"), Fsm7("F#m7"),
    G("G"), Gm("Gm"), G7("G7"), GMaj7("GMaj7"), Gm7("Gm7"),
    Gsf("G#"), Gsm("G#m"), Gs7("G#7"), GsMaj7("G#Maj7"), Gsm7("G#m7");

    companion object {
        fun fromString(value: String): Chord? = entries.find { it.value == value }
    }
}

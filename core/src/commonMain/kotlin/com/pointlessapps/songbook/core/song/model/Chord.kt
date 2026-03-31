package com.pointlessapps.songbook.core.song.model

/**
 * Represents a finger pattern that can slide up and down the neck.
 */
data class ChordShape(
    val id: String,
    val pattern: List<Int?>,
    val rootString: Int,
)

/**
 * A specific instance of a chord used in a song.
 */
data class ChordInstance(
    val rootNote: String,
    val quality: String,
    val shape: ChordShape,
    val octaveOffset: Int = 0,
)

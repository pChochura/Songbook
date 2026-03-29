package com.pointlessapps.songbook.core.setlist.model

import com.pointlessapps.songbook.core.song.model.Song
import kotlinx.serialization.Serializable

@Serializable
data class Setlist(
    val id: Long,
    val name: String,
    val songs: List<Song>,
)

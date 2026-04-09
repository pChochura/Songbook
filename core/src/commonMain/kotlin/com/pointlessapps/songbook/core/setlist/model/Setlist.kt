package com.pointlessapps.songbook.core.setlist.model

import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Setlist(
    val id: Long,
    val name: String,
    val songCount: Int,
)

@Keep
@Serializable
data class NewSetlist(
    val name: String,
)

@Keep
@Serializable
data class SetlistWithSong(
    @SerialName("setlist_id")
    val setlistId: Long,
    @SerialName("song_id")
    val songId: Long,
    val order: Int,
)

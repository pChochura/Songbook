package com.pointlessapps.songbook.core.setlist.model

import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Setlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
)

@Keep
@Serializable
data class NewSetlist(
    val name: String,
)

package com.pointlessapps.songbook.core.setlist.model

import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Setlist(
    val id: String,
    val name: String,
    val songCount: Int = 0,
)

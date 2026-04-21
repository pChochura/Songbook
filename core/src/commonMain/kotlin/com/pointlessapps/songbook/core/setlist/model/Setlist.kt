package com.pointlessapps.songbook.core.setlist.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Stable
@Immutable
data class Setlist(
    val id: String,
    val name: String,
    val songCount: Int = 0,
)

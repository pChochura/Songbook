package com.pointlessapps.songbook.model

import com.pointlessapps.songbook.utils.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class OllamaResponseBody(
    @SerialName("model")
    val model: String,
    @SerialName("response")
    val response: String,
)

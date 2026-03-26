package com.pointlessapps.songbook.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OllamaResponseBody(
    @SerialName("model")
    val model: String,
    @SerialName("response")
    val response: String,
)

package com.pointlessapps.songbook.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class OllamaRequestBody(
    @SerialName("model")
    val model: String,
    @SerialName("prompt")
    val prompt: String,
    @SerialName("stream")
    val stream: Boolean = false,
    @SerialName("think")
    val think: Boolean = false,
    @SerialName("images")
    val images: List<String>,
    @SerialName("format")
    val format: JsonObject,
)

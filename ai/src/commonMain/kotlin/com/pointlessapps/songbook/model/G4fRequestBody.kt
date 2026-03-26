package com.pointlessapps.songbook.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class G4fRequestBody(
    @SerialName("model")
    val model: String,
    @SerialName("provider")
    val provider: String? = null,
    @SerialName("messages")
    val messages: List<Message>,
    @SerialName("images")
    val images: List<List<String>>? = null,
) {
    @Serializable
    data class Message(
        @SerialName("role")
        val role: String,
        @SerialName("content")
        val content: String,
    )
}

package com.pointlessapps.songbook.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class G4fResponseBody(
    @SerialName("choices")
    val choices: List<Choice>,
) {
    @Serializable
    data class Choice(
        @SerialName("message")
        val message: Message,
    ) {
        @Serializable
        data class Message(
            @SerialName("content")
            val content: String,
        )
    }
}

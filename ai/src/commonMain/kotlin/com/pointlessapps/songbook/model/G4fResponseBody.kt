package com.pointlessapps.songbook.model

import com.pointlessapps.songbook.utils.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class G4fResponseBody(
    @SerialName("choices")
    val choices: List<Choice>,
) {
    @Keep
    @Serializable
    data class Choice(
        @SerialName("message")
        val message: Message,
    ) {
        @Keep
        @Serializable
        data class Message(
            @SerialName("content")
            val content: String,
        )
    }
}

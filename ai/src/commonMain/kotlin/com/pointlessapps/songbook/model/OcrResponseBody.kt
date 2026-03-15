package com.pointlessapps.songbook.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OcrResponseBody(
    @SerialName("candidates")
    val candidates: List<Candidate>,
) {
    @Serializable
    data class Candidate(
        @SerialName("content")
        val content: Content,
    ) {
        @Serializable
        data class Content(
            @SerialName("parts")
            val parts: List<Part>,
        ) {
            @Serializable
            data class Part(
                @SerialName("text")
                val text: String,
            )
        }
    }
}

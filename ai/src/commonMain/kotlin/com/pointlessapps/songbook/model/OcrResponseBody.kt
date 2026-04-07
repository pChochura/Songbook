package com.pointlessapps.songbook.model

import com.pointlessapps.songbook.utils.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class OcrResponseBody(
    @SerialName("candidates")
    val candidates: List<Candidate>,
) {
    @Keep
    @Serializable
    data class Candidate(
        @SerialName("content")
        val content: Content,
    ) {
        @Keep
        @Serializable
        data class Content(
            @SerialName("parts")
            val parts: List<Part>,
        ) {
            @Keep
            @Serializable
            data class Part(
                @SerialName("text")
                val text: String,
            )
        }
    }
}

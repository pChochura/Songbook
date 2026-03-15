package com.pointlessapps.songbook.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OcrRequestBody(
    @SerialName("contents")
    val contents: List<Content>,
    @SerialName("generation_config")
    val generationConfig: GenerationConfig,
) {
    @Serializable
    data class Content(
        @SerialName("parts")
        val parts: List<Part>,
    ) {
        @Serializable
        data class Part(
            @SerialName("text")
            val text: String? = null,
            @SerialName("inline_data")
            val inlineData: InlineData? = null,
        ) {
            @Serializable
            data class InlineData(
                @SerialName("mime_type")
                val mimeType: String,
                @SerialName("data")
                val data: String,
            )
        }
    }

    @Serializable
    data class GenerationConfig(
        @SerialName("temperature")
        val temperature: Double,
        @SerialName("topP")
        val topP: Double,
        @SerialName("topK")
        val topK: Int,
        @SerialName("max_output_tokens")
        val maxOutputTokens: Int,
    )
}

package com.pointlessapps.songbook.model

import com.pointlessapps.songbook.utils.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class OcrRequestBody(
    @SerialName("contents")
    val contents: List<Content>,
    @SerialName("generation_config")
    val generationConfig: GenerationConfig,
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
            val text: String? = null,
            @SerialName("inline_data")
            val inlineData: InlineData? = null,
        ) {
            @Keep
            @Serializable
            data class InlineData(
                @SerialName("mime_type")
                val mimeType: String,
                @SerialName("data")
                val data: String,
            )
        }
    }

    @Keep
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

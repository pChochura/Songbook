package com.pointlessapps.songbook.core.song.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Serializable
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val sections: List<Section>,
)

@Serializable
data class NewSong(
    val title: String,
    val artist: String,
    @Serializable(with = SectionListSerializer::class)
    val sections: List<Section>,
) {
    object SectionListSerializer : KSerializer<List<Section>> {
        override val descriptor = PrimitiveSerialDescriptor("SectionList", PrimitiveKind.STRING)

        override fun serialize(
            encoder: Encoder,
            value: List<Section>,
        ) {
            encoder.encodeString(Json.encodeToString(value))
        }

        override fun deserialize(decoder: Decoder): List<Section> =
            Json.decodeFromString(decoder.decodeString())
    }
}

@Serializable
data class Section(
    val name: String,
    val lyrics: String,
    val chords: List<String>,
)

package com.pointlessapps.songbook.data

import androidx.room.TypeConverter
import com.pointlessapps.songbook.core.domain.models.ParsedLine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class Converters {
    @TypeConverter
    fun fromSections(sections: List<List<ParsedLine>>?): String? {
        return sections?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toSections(value: String?): List<List<ParsedLine>>? {
        return value?.let { Json.decodeFromString(it) }
    }
}

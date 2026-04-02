package com.pointlessapps.songbook.core.database

import androidx.room.TypeConverter
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.song.model.Song
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class Converters {
    @TypeConverter
    fun fromSectionList(value: List<Section>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toSectionList(value: String): List<Section> {
        return Json.decodeFromString(value)
    }
}

package com.pointlessapps.songbook.core.database

import androidx.room.TypeConverter
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.sync.database.entity.SyncAction
import kotlinx.serialization.json.Json

internal class Converters {
    private val json = Json {
        serializersModule = SyncAction.SerializersModule
    }

    @TypeConverter
    fun fromSyncAction(value: SyncAction): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toSyncAction(value: String): SyncAction {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromSectionList(value: List<Section>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toSectionList(value: String): List<Section> {
        return Json.decodeFromString(value)
    }
}

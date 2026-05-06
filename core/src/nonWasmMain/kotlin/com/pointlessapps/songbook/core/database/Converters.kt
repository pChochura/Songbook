package com.pointlessapps.songbook.core.database

import androidx.room.TypeConverter
import com.pointlessapps.songbook.core.sync.database.entity.SyncAction
import kotlinx.serialization.json.Json
import kotlin.time.Instant

internal class Converters {
    private val json = Json {
        serializersModule = SyncAction.SerializersModule
        explicitNulls = false
    }

    @TypeConverter
    fun fromSyncAction(value: SyncAction): String = json.encodeToString(value)

    @TypeConverter
    fun toSyncAction(value: String): SyncAction = json.decodeFromString(value)

    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)
}

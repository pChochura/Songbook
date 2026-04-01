package com.pointlessapps.songbook.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.pointlessapps.songbook.core.database.dao.SetlistDao
import com.pointlessapps.songbook.core.database.dao.SongDao
import com.pointlessapps.songbook.core.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        SetlistEntity::class,
    ],
    version = 1,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun setlistDao(): SetlistDao
}

internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

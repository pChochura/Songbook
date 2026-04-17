package com.pointlessapps.songbook.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.pointlessapps.songbook.core.database.dao.SetlistDao
import com.pointlessapps.songbook.core.database.dao.SongDao
import com.pointlessapps.songbook.core.database.dao.SyncDao
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchEntity
import com.pointlessapps.songbook.core.sync.database.dao.SyncActionDao
import com.pointlessapps.songbook.core.sync.database.entity.SyncActionEntity

@Database(
    entities = [
        SongEntity::class,
        SongSearchEntity::class,
        SetlistEntity::class,
        SetlistSongEntity::class,
        SyncActionEntity::class,
    ],
    version = 1,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun setlistDao(): SetlistDao
    abstract fun syncActionDao(): SyncActionDao
    abstract fun syncDao(): SyncDao
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

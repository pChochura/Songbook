package com.pointlessapps.songbook.core.song.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pointlessapps.songbook.core.utils.Keep
import kotlin.time.Instant

@Keep
@Entity(tableName = "songs")
internal data class SongEntity(
    @PrimaryKey val id: String,
    @ColumnInfo("title")
    val title: String,
    @ColumnInfo("artist")
    val artist: String,
    @ColumnInfo("date_added")
    val dateAdded: Instant,
    @ColumnInfo("lyrics")
    val lyrics: String,
)

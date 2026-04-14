package com.pointlessapps.songbook.core.song.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4
@Entity(tableName = "songs_search")
internal data class SongSearchEntity(
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int? = null,
    val songId: String,
    val title: String,
    val artist: String,
    val content: String,
)

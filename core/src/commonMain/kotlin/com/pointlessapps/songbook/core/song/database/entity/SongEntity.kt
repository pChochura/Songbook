package com.pointlessapps.songbook.core.song.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pointlessapps.songbook.core.song.model.Section

@Entity(tableName = "songs")
internal data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val sections: List<Section>,
)

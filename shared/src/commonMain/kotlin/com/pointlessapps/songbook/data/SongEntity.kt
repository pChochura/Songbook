package com.pointlessapps.songbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pointlessapps.songbook.core.domain.models.ParsedLine

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val lyrics: String,
    val key: String? = null,
    val duration: String? = null,
    val bpm: Int? = null,
    val sections: List<List<ParsedLine>>? = null,
)

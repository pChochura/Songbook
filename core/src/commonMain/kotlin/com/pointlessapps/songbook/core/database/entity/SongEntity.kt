package com.pointlessapps.songbook.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.song.model.Song

@Entity(tableName = "songs")
internal data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val sections: List<Section>,
)

internal fun SongEntity.toDomain() = Song(
    id = id,
    title = title,
    artist = artist,
    sections = sections,
)

internal fun Song.toEntity() = SongEntity(
    id = id,
    title = title,
    artist = artist,
    sections = sections,
)

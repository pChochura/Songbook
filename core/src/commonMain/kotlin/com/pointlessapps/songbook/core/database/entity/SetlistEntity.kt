package com.pointlessapps.songbook.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.Song

@Entity(tableName = "setlists")
internal data class SetlistEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val songs: List<Song>,
)

internal fun SetlistEntity.toDomain() = Setlist(
    id = id,
    name = name,
    songs = songs,
)

internal fun Setlist.toEntity() = SetlistEntity(
    id = id,
    name = name,
    songs = songs,
)

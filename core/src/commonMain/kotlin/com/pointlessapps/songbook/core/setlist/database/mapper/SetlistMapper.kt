package com.pointlessapps.songbook.core.setlist.database.mapper

import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistWithSongs
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.database.mapper.toDomain

internal fun SetlistWithSongs.toDomain() = Setlist(
    id = setlist.id,
    name = setlist.name,
    songs = songs.map { it.toDomain() },
)

internal fun Setlist.toEntity() = SetlistEntity(
    id = id,
    name = name,
)

internal fun Setlist.toSongEntities() = songs.map {
    SetlistSongEntity(
        setlistId = id,
        songId = it.id,
    )
}

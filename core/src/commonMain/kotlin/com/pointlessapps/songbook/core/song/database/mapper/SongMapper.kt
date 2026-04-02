package com.pointlessapps.songbook.core.song.database.mapper

import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.model.Song

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

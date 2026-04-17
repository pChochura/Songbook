package com.pointlessapps.songbook.core.song.database.mapper

import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchEntity
import com.pointlessapps.songbook.core.song.model.Section.Companion.toLyrics
import com.pointlessapps.songbook.core.song.model.Song

internal fun SongEntity.toDomain() = Song(
    id = id,
    title = title,
    artist = artist,
    sections = sections,
)

internal fun SongEntity.toSearchEntity() = SongSearchEntity(
    songId = id,
    title = title,
    artist = artist,
    content = sections.toLyrics(withChords = false),
)

internal fun Song.toSearchEntity() = SongSearchEntity(
    songId = id,
    title = title,
    artist = artist,
    content = sections.toLyrics(withChords = false),
)

internal fun Song.toEntity() = SongEntity(
    id = id,
    title = title,
    artist = artist,
    sections = sections,
)

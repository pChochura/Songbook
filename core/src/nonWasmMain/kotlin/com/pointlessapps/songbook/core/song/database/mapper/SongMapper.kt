package com.pointlessapps.songbook.core.song.database.mapper

import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchEntity
import com.pointlessapps.songbook.core.song.model.Song
import kotlin.time.Clock

internal fun SongEntity.toDomain() = Song(
    id = id,
    title = title,
    artist = artist,
    dateAdded = dateAdded,
    lyrics = lyrics,
)

internal fun SongEntity.toSearchEntity() = SongSearchEntity(
    songId = id,
    title = title,
    artist = artist,
    plainLyrics = lyrics,
    content = lyrics.replace(Regex("\\[.*?]"), ""),
)

internal fun Song.toEntity() = SongEntity(
    id = id,
    title = title,
    artist = artist,
    dateAdded = dateAdded ?: Clock.System.now(),
    lyrics = lyrics,
)

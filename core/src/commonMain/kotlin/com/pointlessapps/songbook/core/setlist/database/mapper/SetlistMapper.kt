package com.pointlessapps.songbook.core.setlist.database.mapper

import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistWithCount
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.Song

internal fun SetlistWithCount.toDomain() = Setlist(
    id = setlist.id,
    name = setlist.name,
    songCount = songCount,
)

internal fun Setlist.toEntity() = SetlistEntity(
    id = id,
    name = name,
)

internal fun List<Song>.toEntities(setlistId: Long) = mapIndexed { index, song ->
    SetlistSongEntity(
        setlistId = setlistId,
        songId = song.id,
        order = index,
    )
}

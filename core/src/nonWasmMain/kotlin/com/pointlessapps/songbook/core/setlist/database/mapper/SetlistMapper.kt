package com.pointlessapps.songbook.core.setlist.database.mapper

import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistWithCount
import com.pointlessapps.songbook.core.setlist.model.Setlist

internal fun SetlistWithCount.toDomain() = Setlist(
    id = setlist.id,
    name = setlist.name,
    songCount = songCount,
)

internal fun SetlistEntity.toDomain() = Setlist(
    id = id,
    name = name,
)

internal fun Setlist.toEntity() = SetlistEntity(
    id = id,
    name = name,
)

package com.pointlessapps.songbook.library.ui.mapper

import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.library.SortBy

internal fun SortBy.Field.toDomain() = when (this) {
    SortBy.Field.Title -> Song.SortBy.Title
    SortBy.Field.Artist -> Song.SortBy.Artist
    SortBy.Field.DateAdded -> Song.SortBy.DateAdded
}

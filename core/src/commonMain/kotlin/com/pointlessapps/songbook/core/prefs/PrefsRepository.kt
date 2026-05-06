package com.pointlessapps.songbook.core.prefs

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.Flow

interface PrefsRepository {
    fun getLastSearchesFlow(): Flow<ImmutableSet<String>>
    suspend fun addLastSearch(search: String)
    suspend fun removeLastSearch(search: String)

    fun getLyricsTextScaleFlow(): Flow<Int>
    suspend fun setLyricsTextScale(scale: Int)

    fun getLyricsDisplayModeFlow(): Flow<String?>
    suspend fun setLyricsDisplayMode(mode: String)

    fun getLibraryDisplayModeFlow(): Flow<String?>
    suspend fun setLibraryDisplayMode(mode: String)

    fun getLyricsWrapModeFlow(): Flow<String?>
    suspend fun setLyricsWrapMode(mode: String)

    fun getShowKeyOffsetFabFlow(): Flow<Boolean>
    suspend fun setShowKeyOffsetFab(show: Boolean)

    suspend fun getShowPublicLyrics(): Boolean?
    suspend fun setShowPublicLyrics(show: Boolean)

    fun getLibrarySortByFlow(): Flow<Pair<String, Boolean>?>
    suspend fun setLibrarySortBy(field: String, ascending: Boolean)
}

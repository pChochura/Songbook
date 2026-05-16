package com.pointlessapps.songbook.core.prefs

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class WasmPrefsRepository : PrefsRepository {
    override fun getLastSearchesFlow(): Flow<ImmutableSet<String>> = emptyFlow()
    override suspend fun addLastSearch(search: String) {}
    override suspend fun removeLastSearch(search: String) {}
    override fun getLyricsTextScaleFlow(): Flow<Int> = emptyFlow()
    override suspend fun setLyricsTextScale(scale: Int) {}
    override fun getLyricsDisplayModeFlow(): Flow<String?> = emptyFlow()
    override suspend fun setLyricsDisplayMode(mode: String) {}
    override fun getLibraryDisplayModeFlow(): Flow<String?> = emptyFlow()
    override suspend fun setLibraryDisplayMode(mode: String) {}
    override fun getLyricsWrapModeFlow(): Flow<String?> = emptyFlow()
    override suspend fun setLyricsWrapMode(mode: String) {}
    override fun getShowKeyOffsetFabFlow(): Flow<Boolean> = emptyFlow()
    override suspend fun setShowKeyOffsetFab(show: Boolean) {}
    override suspend fun getShowPublicLyrics(): Boolean? = null
    override suspend fun setShowPublicLyrics(show: Boolean) {}
    override fun getLibrarySortByFlow(): Flow<Pair<String, Boolean>?> = emptyFlow()
    override suspend fun setLibrarySortBy(field: String, ascending: Boolean) {}

    override suspend fun clearData() {}
}

package com.pointlessapps.songbook.core.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface PrefsRepository {
    fun getLastSearchesFlow(): Flow<Set<String>>
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

    suspend fun getShowPublicLyrics(): Boolean?
    suspend fun setShowPublicLyrics(show: Boolean)
}

internal class PrefsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : PrefsRepository {
    private val lastSearchesKey = stringSetPreferencesKey("last_searches")
    private val lyricsTextScaleKey = intPreferencesKey("lyrics_text_scale")
    private val lyricsDisplayModeKey = stringPreferencesKey("lyrics_display_mode")
    private val libraryDisplayModeKey = stringPreferencesKey("library_display_mode")
    private val lyricsWrapModeKey = stringPreferencesKey("lyrics_wrap_mode")
    private val showPublicLyricsKey = booleanPreferencesKey("show_public_lyrics")

    override fun getLastSearchesFlow() = dataStore.data.map { preferences ->
        preferences[lastSearchesKey].orEmpty()
    }.flowOn(Dispatchers.IO)

    override suspend fun addLastSearch(search: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    val searches = get(lastSearchesKey).orEmpty().take(LAST_SEARCHES_NUMBER - 1)
                    set(lastSearchesKey, setOf(search) + searches)
                }
            }
        }
    }

    override suspend fun removeLastSearch(search: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    val searches = get(lastSearchesKey).orEmpty()
                    set(lastSearchesKey, searches - search)
                }
            }
        }
    }

    override fun getLyricsTextScaleFlow() = dataStore.data.map { preferences ->
        preferences[lyricsTextScaleKey] ?: 100
    }.flowOn(Dispatchers.IO)

    override suspend fun setLyricsTextScale(scale: Int) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    set(lyricsTextScaleKey, scale)
                }
            }
        }
    }

    override fun getLyricsDisplayModeFlow() = dataStore.data.map { preferences ->
        preferences[lyricsDisplayModeKey]
    }.flowOn(Dispatchers.IO)

    override suspend fun setLyricsDisplayMode(mode: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    set(lyricsDisplayModeKey, mode)
                }
            }
        }
    }

    override fun getLibraryDisplayModeFlow() = dataStore.data.map { preferences ->
        preferences[libraryDisplayModeKey]
    }.flowOn(Dispatchers.IO)

    override suspend fun setLibraryDisplayMode(mode: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    set(libraryDisplayModeKey, mode)
                }
            }
        }
    }

    override fun getLyricsWrapModeFlow() = dataStore.data.map { preferences ->
        preferences[lyricsWrapModeKey]
    }.flowOn(Dispatchers.IO)

    override suspend fun setLyricsWrapMode(mode: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    set(lyricsWrapModeKey, mode)
                }
            }
        }
    }

    override suspend fun getShowPublicLyrics() = withContext(Dispatchers.IO) {
        dataStore.data.map { preferences ->
            preferences[showPublicLyricsKey]
        }.first()
    }

    override suspend fun setShowPublicLyrics(show: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    set(showPublicLyricsKey, show)
                }
            }
        }
    }

    private companion object {
        const val LAST_SEARCHES_NUMBER = 5
    }
}

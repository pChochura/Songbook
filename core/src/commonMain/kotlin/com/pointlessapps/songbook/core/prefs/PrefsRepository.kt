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

    fun getTextScaleFlow(): Flow<Int>
    suspend fun setTextScale(scale: Int)

    fun getDisplayModeFlow(): Flow<String?>
    suspend fun setDisplayMode(mode: String)

    fun getWrapModeFlow(): Flow<String?>
    suspend fun setWrapMode(mode: String)

    suspend fun getShowPublicLyrics(): Boolean?
    suspend fun setShowPublicLyrics(show: Boolean)
}

internal class PrefsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : PrefsRepository {
    private val lastSearchesKey = stringSetPreferencesKey("last_searches")
    private val textScaleKey = intPreferencesKey("text_scale")
    private val displayModeKey = stringPreferencesKey("display_mode")
    private val wrapModeKey = stringPreferencesKey("wrap_mode")
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

    override fun getTextScaleFlow() = dataStore.data.map { preferences ->
        preferences[textScaleKey] ?: 100
    }.flowOn(Dispatchers.IO)

    override suspend fun setTextScale(scale: Int) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    set(textScaleKey, scale)
                }
            }
        }
    }

    override fun getDisplayModeFlow() = dataStore.data.map { preferences ->
        preferences[displayModeKey]
    }.flowOn(Dispatchers.IO)

    override suspend fun setDisplayMode(mode: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    set(displayModeKey, mode)
                }
            }
        }
    }

    override fun getWrapModeFlow() = dataStore.data.map { preferences ->
        preferences[wrapModeKey]
    }.flowOn(Dispatchers.IO)

    override suspend fun setWrapMode(mode: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    set(wrapModeKey, mode)
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

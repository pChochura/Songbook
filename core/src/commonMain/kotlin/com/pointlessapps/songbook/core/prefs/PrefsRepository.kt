package com.pointlessapps.songbook.core.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface PrefsRepository {
    suspend fun getLastSearchesFlow(): Flow<Set<String>>
    suspend fun addLastSearch(search: String)
    suspend fun removeLastSearch(search: String)

    suspend fun getTextScale(): Float
    suspend fun setTextScale(scale: Float)

    suspend fun getMode(): String?
    suspend fun setMode(mode: String)
}

internal class PrefsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : PrefsRepository {
    private val lastSearchesKey = stringSetPreferencesKey("last_searches")
    private val textScaleKey = floatPreferencesKey("text_scale")
    private val modeKey = stringPreferencesKey("mode")

    override suspend fun getLastSearchesFlow() = withContext(Dispatchers.IO) {
        dataStore.data.map { preferences ->
            preferences[lastSearchesKey].orEmpty()
        }
    }

    override suspend fun addLastSearch(search: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    val searches = get(lastSearchesKey).orEmpty().take(LAST_SEARCHES_NUMBER - 1)
                    set(lastSearchesKey, (searches + search).toSet())
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

    override suspend fun getTextScale() = withContext(Dispatchers.IO) {
        dataStore.data.map { preferences ->
            preferences[textScaleKey] ?: 1f
        }.first()
    }

    override suspend fun setTextScale(scale: Float) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    set(textScaleKey, scale)
                }
            }
        }
    }

    override suspend fun getMode() = withContext(Dispatchers.IO) {
        dataStore.data.map { preferences ->
            preferences[modeKey]
        }.first()
    }

    override suspend fun setMode(mode: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData {
                it.toMutablePreferences().apply {
                    set(modeKey, mode)
                }
            }
        }
    }

    private companion object {
        const val LAST_SEARCHES_NUMBER = 5
    }
}

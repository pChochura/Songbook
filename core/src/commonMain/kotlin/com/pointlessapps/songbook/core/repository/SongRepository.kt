package com.pointlessapps.songbook.core.repository

import com.pointlessapps.songbook.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    suspend fun getSongById(id: Long): Song?
    suspend fun saveSong(song: Song): Long
    suspend fun deleteSong(id: Long)
}

internal class SongRepositoryImpl : SongRepository {
    override fun getAllSongs(): Flow<List<Song>> {
        return emptyFlow()
    }

    override suspend fun getSongById(id: Long): Song? {
        return null
    }

    override suspend fun saveSong(song: Song): Long {
        return 0
    }

    override suspend fun deleteSong(id: Long) {
    }
}

package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.Song
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface SongRepository {
    suspend fun getAllSongs(): List<Song>
    suspend fun getSongById(id: Long): Song?
    suspend fun saveSong(song: Song): Long
    suspend fun deleteSong(id: Long)
}

internal class SongRepositoryImpl(
    supabase: SupabaseClient,
) : SongRepository {

    private val songsTable = supabase.from("songs")

    override suspend fun getAllSongs() = withContext(Dispatchers.IO) {
        songsTable.select().decodeList<Song>()
    }

    override suspend fun getSongById(id: Long) = withContext(Dispatchers.IO) {
        songsTable.select {
            filter { Song::id eq id }
        }.decodeSingleOrNull<Song>()
    }

    override suspend fun saveSong(song: Song) = withContext(Dispatchers.IO) {
        songsTable.insert(song).decodeSingle<Song>().id
    }

    override suspend fun deleteSong(id: Long) {
        withContext(Dispatchers.IO) {
            songsTable.delete { filter { Song::id eq id } }
        }
    }
}

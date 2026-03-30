package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Song
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface SongRepository {
    suspend fun getAllSongs(): List<Song>
    suspend fun getSongById(id: Long): Song?
    suspend fun saveSong(newSong: NewSong)
    suspend fun deleteSong(id: Long)
}

internal class SongRepositoryImpl(
    supabase: SupabaseClient,
) : SongRepository {

    private val table = supabase.from("songs")

    override suspend fun getAllSongs() = withContext(Dispatchers.IO) {
        table.select().decodeList<Song>()
    }

    override suspend fun getSongById(id: Long) = withContext(Dispatchers.IO) {
        table.select {
            filter { Song::id eq id }
        }.decodeSingleOrNull<Song>()
    }

    override suspend fun saveSong(newSong: NewSong) {
        withContext(Dispatchers.IO) {
            table.insert(newSong)
        }
    }

    override suspend fun deleteSong(id: Long) {
        withContext(Dispatchers.IO) {
            table.delete { filter { Song::id eq id } }
        }
    }
}

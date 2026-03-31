package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Song
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getSongById(id: Long): Flow<Song?>
    suspend fun saveSong(newSong: NewSong)
    suspend fun deleteSong(id: Long)
}

@OptIn(SupabaseExperimental::class)
internal class SongRepositoryImpl(
    supabase: SupabaseClient,
) : SongRepository {

    private val table = supabase.from("songs")

    override fun getAllSongs() = table.selectAsFlow(Song::id)
        .flowOn(Dispatchers.IO)

    override fun getSongById(id: Long) = table.selectSingleValueAsFlow(Song::id) {
        Song::id eq id
    }.flowOn(Dispatchers.IO)

    override suspend fun saveSong(newSong: NewSong) {
        withContext(Dispatchers.IO) {
            table.insert(newSong)
        }
    }

    override suspend fun deleteSong(id: Long) {
        // TODO mark as deleted instead of removing
        withContext(Dispatchers.IO) {
            table.delete { filter { Song::id eq id } }
        }
    }
}

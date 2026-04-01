package com.pointlessapps.songbook.core.song

import com.pointlessapps.songbook.core.database.dao.SongDao
import com.pointlessapps.songbook.core.database.entity.toDomain
import com.pointlessapps.songbook.core.database.entity.toEntity
import com.pointlessapps.songbook.core.model.DataState
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Song
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

interface SongRepository {
    fun getAllSongs(): Flow<DataState<List<Song>>>
    fun getSongById(id: Long): Flow<DataState<Song?>>
    suspend fun saveSong(newSong: NewSong)
    suspend fun deleteSong(id: Long)
}

@OptIn(SupabaseExperimental::class)
internal class SongRepositoryImpl(
    supabase: SupabaseClient,
    private val songDao: SongDao,
) : SongRepository {

    private val table = supabase.from("songs")

    override fun getAllSongs(): Flow<DataState<List<Song>>> = flow {
        val remoteFlow = table.selectAsFlow(Song::id)
            .onEach { songs ->
                songDao.insertSongs(songs.map { it.toEntity() })
            }
            .map { SyncStatus.SYNCED as SyncStatus? }
            .onStart { emit(null) }

        val localFlow = songDao.getAllSongs()
            .map { entities -> entities.map { it.toDomain() } }

        val combinedFlow = combine(localFlow, remoteFlow) { data, status ->
            DataState(data, status ?: SyncStatus.LOCAL)
        }

        emitAll(combinedFlow)
    }.flowOn(Dispatchers.IO)

    override fun getSongById(id: Long) = songDao.getSongById(id)
        .map { DataState(it?.toDomain(), SyncStatus.LOCAL) }

    override suspend fun saveSong(newSong: NewSong) {
        withContext(Dispatchers.IO) {
            table.insert(newSong)
        }
    }

    override suspend fun deleteSong(id: Long) {
        // TODO mark as deleted instead of removing
        withContext(Dispatchers.IO) {
            table.delete { filter { Song::id eq id } }
            songDao.deleteSong(id)
        }
    }
}

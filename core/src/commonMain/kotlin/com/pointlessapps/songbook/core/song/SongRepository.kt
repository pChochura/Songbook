package com.pointlessapps.songbook.core.song

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pointlessapps.songbook.core.database.dao.SongDao
import com.pointlessapps.songbook.core.model.DataState
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.song.database.entity.SongSearchResult
import com.pointlessapps.songbook.core.song.database.mapper.toDomain
import com.pointlessapps.songbook.core.song.database.mapper.toEntity
import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Song
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

interface SongRepository {
    fun getAllSongsFlow(): Flow<DataState<List<Song>>>
    fun getSongByIdFlow(id: Long): Flow<DataState<Song?>>
    fun searchSongsFlow(query: String): Flow<PagingData<SongSearchResult>>

    suspend fun saveSong(newSong: NewSong)
    suspend fun deleteSong(id: Long)
}

@OptIn(SupabaseExperimental::class)
internal class SongRepositoryImpl(
    supabase: SupabaseClient,
    private val songDao: SongDao,
) : SongRepository {

    private val table = supabase.from("songs")

    override fun getAllSongsFlow(): Flow<DataState<List<Song>>> = flow {
        val remoteFlow = table.selectAsFlow(Song::id)
            .onEach { songs ->
                songDao.insertSongsWithSearch(songs.map { it.toEntity() })
            }
            .map { SyncStatus.SYNCED as SyncStatus? }
            .catch { emit(SyncStatus.REMOTE_FAILED) }
            .onStart { emit(null) }
            .flowOn(Dispatchers.IO)

        val localFlow = songDao.getAllSongs()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

        val combinedFlow = combine(localFlow, remoteFlow) { data, status ->
            DataState(data, status ?: SyncStatus.LOCAL)
        }

        emitAll(combinedFlow)
    }.flowOn(Dispatchers.IO)

    override fun getSongByIdFlow(id: Long) = songDao.getSongById(id)
        .map { DataState(it?.toDomain(), SyncStatus.LOCAL) }
        .flowOn(Dispatchers.IO)

    override fun searchSongsFlow(query: String): Flow<PagingData<SongSearchResult>> {
        if (query.isBlank()) return flowOf(PagingData.empty())

        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { songDao.searchSongs("$query*") },
        ).flow
    }

    override suspend fun saveSong(newSong: NewSong) {
        withContext(Dispatchers.IO) {
            table.upsert(newSong)
        }
    }

    override suspend fun deleteSong(id: Long) {
        // TODO mark as deleted instead of removing
        withContext(Dispatchers.IO) {
            table.delete { filter { Song::id eq id } }
            songDao.deleteSongWithSearch(id)
        }
    }
}

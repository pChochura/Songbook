package com.pointlessapps.songbook.core.setlist

import com.pointlessapps.songbook.core.database.dao.SetlistDao
import com.pointlessapps.songbook.core.database.dao.SongDao
import com.pointlessapps.songbook.core.model.DataState
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistWithCount
import com.pointlessapps.songbook.core.setlist.database.mapper.toDomain
import com.pointlessapps.songbook.core.setlist.database.mapper.toEntities
import com.pointlessapps.songbook.core.setlist.database.mapper.toEntity
import com.pointlessapps.songbook.core.setlist.model.NewSetlist
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.mapper.toDomain
import com.pointlessapps.songbook.core.song.database.mapper.toEntity
import com.pointlessapps.songbook.core.song.model.Song
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

interface SetlistRepository {
    fun getAllSetlistsFlow(limit: Long = -1L): Flow<DataState<List<Setlist>>>
    fun getSetlistByIdFlow(id: Long): Flow<DataState<Setlist?>>
    fun getSetlistsSongsByIdFlow(id: Long): Flow<DataState<List<Song>>>

    suspend fun addSetlist(name: String): Long
    suspend fun deleteSetlist(id: Long)
    suspend fun updateSetlistName(id: Long, name: String)
    suspend fun updateSetlistSongsOrder(id: Long, songs: List<Song>)
}

@OptIn(SupabaseExperimental::class)
internal class SetlistRepositoryImpl(
    supabase: SupabaseClient,
    private val setlistDao: SetlistDao,
    private val songDao: SongDao,
) : SetlistRepository {

    private val songsTable = supabase.from("songs")
    private val setlistsTable = supabase.from("setlists")
    private val setlistSongsTable = supabase.from("setlist_songs")

    override fun getAllSetlistsFlow(limit: Long): Flow<DataState<List<Setlist>>> = flow {
        val remoteFlow = setlistsTable.selectAsFlow(Setlist::id)
            .onEach { setlistDao.insertSetlists(it.map(Setlist::toEntity)) }
            .map { SyncStatus.SYNCED as SyncStatus? }
            .catch { emit(SyncStatus.REMOTE_FAILED) }
            .onStart { emit(null) }
            .flowOn(Dispatchers.IO)

        val localFlow = setlistDao.getAllSetlistsFlow(limit)
            .map { it.map(SetlistWithCount::toDomain) }
            .flowOn(Dispatchers.IO)

        val combinedFlow = combine(localFlow, remoteFlow) { data, status ->
            DataState(data, status ?: SyncStatus.LOCAL)
        }

        emitAll(combinedFlow)
    }.flowOn(Dispatchers.IO)

    override fun getSetlistByIdFlow(id: Long) = setlistDao.getSetlistByIdFlow(id)
        .map { DataState(it?.toDomain(), SyncStatus.LOCAL) }
        .flowOn(Dispatchers.IO)

    override fun getSetlistsSongsByIdFlow(id: Long): Flow<DataState<List<Song>>> = flow {
        val remoteFlow = setlistSongsTable.selectAsFlow(
            primaryKey = SetlistSongEntity::songId,
            filter = FilterOperation("setlistId", FilterOperator.EQ, id),
        ).onEach { setlistSongs ->
            setlistDao.insertSetlistSongs(setlistSongs)
            val songIds = setlistSongs.map { it.songId }
            if (songIds.isNotEmpty()) {
                val songs = songsTable.select {
                    filter { Song::id isIn songIds }
                }.decodeList<Song>()
                songDao.insertSongsWithSearch(songs.map(Song::toEntity))
            }
        }.map { SyncStatus.SYNCED as SyncStatus? }
            .onStart { emit(null) }
            .catch { emit(SyncStatus.REMOTE_FAILED) }
            .flowOn(Dispatchers.IO)

        val localFlow = setlistDao.getSetlistSongsByIdFlow(id)
            .map { it.map(SongEntity::toDomain) }
            .flowOn(Dispatchers.IO)

        val combinedFlow = combine(localFlow, remoteFlow) { data, status ->
            DataState(data, status ?: SyncStatus.LOCAL)
        }

        emitAll(combinedFlow)
    }.flowOn(Dispatchers.IO)

    override suspend fun addSetlist(name: String) = withContext(Dispatchers.IO) {
        setlistsTable.upsert(NewSetlist(name)) { select() }.decodeSingle<SetlistEntity>().id
    }

    override suspend fun deleteSetlist(id: Long) {
        withContext(Dispatchers.IO) {
            setlistsTable.delete { filter { Setlist::id eq id } }
            setlistDao.deleteSetlist(id)
        }
    }

    override suspend fun updateSetlistName(id: Long, name: String) {
        withContext(Dispatchers.IO) {
            setlistsTable.update(
                update = { Setlist::name setTo name },
                request = { filter { Setlist::id eq id } },
            )
            setlistDao.updateSetlistName(id, name)
        }
    }

    override suspend fun updateSetlistSongsOrder(id: Long, songs: List<Song>) {
        withContext(Dispatchers.IO) {
            val entities = songs.toEntities(id)
            setlistSongsTable.upsert(entities)
            setlistDao.insertSetlistSongs(entities)
        }
    }
}

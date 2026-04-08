package com.pointlessapps.songbook.core.setlist

import com.pointlessapps.songbook.core.database.dao.SetlistDao
import com.pointlessapps.songbook.core.database.dao.SongDao
import com.pointlessapps.songbook.core.model.DataState
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistWithSongs
import com.pointlessapps.songbook.core.setlist.database.mapper.toDomain
import com.pointlessapps.songbook.core.setlist.database.mapper.toEntity
import com.pointlessapps.songbook.core.setlist.database.mapper.toSongEntities
import com.pointlessapps.songbook.core.setlist.model.NewSetlist
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.Song
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlin.random.Random
import com.pointlessapps.songbook.core.song.database.mapper.toEntity as toSongEntity

interface SetlistRepository {
    fun getAllSetlistsFlow(limit: Long = -1L): Flow<DataState<List<Setlist>>>
    fun getSetlistByIdFlow(id: Long): Flow<DataState<Setlist?>>

    suspend fun addSetlist(name: String): Long
    suspend fun deleteSetlist(id: Long)
    suspend fun updateSetlist(setlist: Setlist)
}

@OptIn(SupabaseExperimental::class)
internal class SetlistRepositoryImpl(
    supabase: SupabaseClient,
    private val setlistDao: SetlistDao,
    private val songDao: SongDao,
) : SetlistRepository {

    private val realtime = supabase.realtime
    private val table = supabase.from("setlists")

    override fun getAllSetlistsFlow(limit: Long): Flow<DataState<List<Setlist>>> = flow {
        val channel = realtime.channel("setlists_all_${Random.nextLong()}")
        val setlistChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "setlists"
        }
        val setlistSongsChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "setlist_songs"
        }
        channel.subscribe()

        val remoteFlow = merge(
            setlistSongsChanges,
            setlistChanges,
        ).map {
            syncRemoteData(limit)
            SyncStatus.SYNCED as SyncStatus?
        }.onStart {
            emit(null)
            syncRemoteData(limit)
            emit(SyncStatus.SYNCED)
        }.catch { emit(SyncStatus.REMOTE_FAILED) }
            .flowOn(Dispatchers.IO)

        val localFlow = setlistDao.getAllSetlistsFlow(limit)
            .map { it.map(SetlistWithSongs::toDomain) }
            .flowOn(Dispatchers.IO)

        val combinedFlow = combine(localFlow, remoteFlow) { data, status ->
            DataState(data, status ?: SyncStatus.LOCAL)
        }

        try {
            emitAll(combinedFlow)
        } finally {
            realtime.removeChannel(channel)
        }
    }.flowOn(Dispatchers.IO)

    override fun getSetlistByIdFlow(id: Long): Flow<DataState<Setlist?>> = setlistDao.getSetlistByIdFlow(id)
        .map { DataState(it?.toDomain(), SyncStatus.LOCAL) }
        .flowOn(Dispatchers.IO)

    private suspend fun syncRemoteData(limit: Long) {
        val remoteData = fetchSetlistsWithSongs(limit)
        songDao.insertSongs(remoteData.flatMap(Setlist::songs).map(Song::toSongEntity))
        setlistDao.syncSetlists(
            setlists = remoteData.map(Setlist::toEntity),
            setlistSongs = remoteData.flatMap(Setlist::toSongEntities),
        )
    }

    override suspend fun addSetlist(name: String) = withContext(Dispatchers.IO) {
        table.upsert(NewSetlist(name)) { select() }.decodeSingle<SetlistEntity>().id
    }

    override suspend fun deleteSetlist(id: Long) {
        withContext(Dispatchers.IO) {
            table.delete { filter { Setlist::id eq id } }
            setlistDao.deleteSetlist(id)
        }
    }

    override suspend fun updateSetlist(setlist: Setlist) {
        withContext(Dispatchers.IO) {
            val entity = setlist.toEntity()
            table.upsert(entity)
            setlistDao.updateSetlist(entity)
        }
    }

    private suspend fun fetchSetlistsWithSongs(limit: Long): List<Setlist> = table
        .select(Columns.raw("id, name, songs(*)")) { limit(limit) }
        .decodeList<Setlist>()
}

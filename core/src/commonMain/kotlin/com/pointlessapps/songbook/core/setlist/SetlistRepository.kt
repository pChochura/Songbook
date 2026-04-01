package com.pointlessapps.songbook.core.setlist

import com.pointlessapps.songbook.core.database.dao.SetlistDao
import com.pointlessapps.songbook.core.database.entity.toDomain
import com.pointlessapps.songbook.core.database.entity.toEntity
import com.pointlessapps.songbook.core.model.DataState
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.setlist.model.Setlist
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlin.random.Random

interface SetlistRepository {
    fun getAllSetlists(): Flow<DataState<List<Setlist>>>
    fun getSetlistById(id: Long): Flow<DataState<Setlist?>>
}

@OptIn(SupabaseExperimental::class)
internal class SetlistRepositoryImpl(
    supabase: SupabaseClient,
    private val setlistDao: SetlistDao,
) : SetlistRepository {

    private val realtime = supabase.realtime
    private val table = supabase.from("setlists")

    override fun getAllSetlists(): Flow<DataState<List<Setlist>>> = flow {
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
            val remoteData = fetchSetlistsWithSongs()
            setlistDao.insertSetlists(remoteData.map { it.toEntity() })
            SyncStatus.SYNCED as SyncStatus?
        }.onStart { emit(null) }

        val localFlow = setlistDao.getAllSetlists()
            .map { entities -> entities.map { it.toDomain() } }

        val combinedFlow = combine(localFlow, remoteFlow) { data, status ->
            DataState(data, status ?: SyncStatus.LOCAL)
        }

        try {
            emitAll(combinedFlow)
        } finally {
            realtime.removeChannel(channel)
        }
    }.flowOn(Dispatchers.IO)

    override fun getSetlistById(id: Long): Flow<DataState<Setlist?>> = setlistDao.getSetlistById(id)
        .map { DataState(it?.toDomain(), SyncStatus.LOCAL) }

    private suspend fun fetchSetlistsWithSongs(): List<Setlist> = table.select(
        Columns.raw("id, name, songs(*)"),
    ).decodeList<Setlist>()

    private suspend fun fetchSetlistByIdWithSongs(id: Long): Setlist? = table.select(
        Columns.raw("id, name, songs(*)"),
    ) {
        filter { Setlist::id eq id }
    }.decodeSingleOrNull<Setlist>()
}

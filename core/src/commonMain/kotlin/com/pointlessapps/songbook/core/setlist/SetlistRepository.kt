package com.pointlessapps.songbook.core.setlist

import com.pointlessapps.songbook.core.setlist.model.Setlist
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion

interface SetlistRepository {
    suspend fun getAllSetlists(): Flow<List<Setlist>>
    suspend fun getSetlistById(id: Long): Flow<Setlist?>
}

@OptIn(SupabaseExperimental::class)
internal class SetlistRepositoryImpl(
    supabase: SupabaseClient,
) : SetlistRepository {

    private val realtime = supabase.realtime
    private val table = supabase.from("setlists")

    override suspend fun getAllSetlists(): Flow<List<Setlist>> {
        val channel = realtime.channel("setlists_all")
        return flow {
            val setlistChanges = channel.postgresChangeFlow<PostgresAction>("public") {
                table = "setlists"
            }
            val setlistSongsChanges = channel.postgresChangeFlow<PostgresAction>("public") {
                table = "setlist_songs"
            }
            channel.subscribe()

            emit(fetchSetlistsWithSongs())
            merge(
                setlistSongsChanges,
                setlistChanges,
            ).collect {
                emit(fetchSetlistsWithSongs())
            }
        }.onCompletion {
            realtime.removeChannel(channel)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getSetlistById(id: Long): Flow<Setlist?> {
        val channel = realtime.channel("setlist_$id")
        return flow {
            val changes = channel.postgresChangeFlow<PostgresAction>("public") {
                table = "setlists"
                filter("id", FilterOperator.EQ, id)
            }
            channel.subscribe()

            emit(fetchSetlistByIdWithSongs(id))
            changes.collect {
                emit(fetchSetlistByIdWithSongs(id))
            }
        }.onCompletion {
            realtime.removeChannel(channel)
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun fetchSetlistsWithSongs(): List<Setlist> = table.select(
        Columns.raw("id, name, songs(*)"),
    ).decodeList<Setlist>()

    private suspend fun fetchSetlistByIdWithSongs(id: Long): Setlist? = table.select(
        Columns.raw("id, name, songs(*)"),
    ) {
        filter { Setlist::id eq id }
    }.decodeSingleOrNull<Setlist>()
}

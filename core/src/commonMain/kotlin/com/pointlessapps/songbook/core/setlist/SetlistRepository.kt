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
import kotlin.random.Random

interface SetlistRepository {
    fun getAllSetlists(): Flow<List<Setlist>>
    fun getSetlistById(id: Long): Flow<Setlist?>
}

@OptIn(SupabaseExperimental::class)
internal class SetlistRepositoryImpl(
    supabase: SupabaseClient,
) : SetlistRepository {

    private val realtime = supabase.realtime
    private val table = supabase.from("setlists")

    override fun getAllSetlists(): Flow<List<Setlist>> = flow {
        val channel = realtime.channel("setlists_all_${Random.nextLong()}")
        val setlistChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "setlists"
        }
        val setlistSongsChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "setlist_songs"
        }
        channel.subscribe()

        try {
            emit(fetchSetlistsWithSongs())
            merge(
                setlistSongsChanges,
                setlistChanges,
            ).collect {
                emit(fetchSetlistsWithSongs())
            }
        } finally {
            realtime.removeChannel(channel)
        }
    }.flowOn(Dispatchers.IO)

    override fun getSetlistById(id: Long): Flow<Setlist?> = flow {
        val channel = realtime.channel("setlist_${id}_${Random.nextLong()}")
        val changes = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "setlists"
            filter("id", FilterOperator.EQ, id)
        }
        channel.subscribe()

        try {
            emit(fetchSetlistByIdWithSongs(id))
            changes.collect {
                emit(fetchSetlistByIdWithSongs(id))
            }
        } finally {
            realtime.removeChannel(channel)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun fetchSetlistsWithSongs(): List<Setlist> = table.select(
        Columns.raw("id, name, songs(*)"),
    ).decodeList<Setlist>()

    private suspend fun fetchSetlistByIdWithSongs(id: Long): Setlist? = table.select(
        Columns.raw("id, name, songs(*)"),
    ) {
        filter { Setlist::id eq id }
    }.decodeSingleOrNull<Setlist>()
}

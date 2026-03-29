package com.pointlessapps.songbook.core.setlist

import com.pointlessapps.songbook.core.setlist.model.Setlist
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface SetlistRepository {
    suspend fun getAllSetlists(): List<Setlist>
    suspend fun getSetlistById(id: Long): Setlist?
}

internal class SetlistRepositoryImpl(
    supabase: SupabaseClient,
) : SetlistRepository {

    private val table = supabase.from("setlists")

    override suspend fun getAllSetlists() = withContext(Dispatchers.IO) {
        table.select(Columns.raw("""
            id,
            name,
            songs(*)
        """.trimIndent())).decodeList<Setlist>()
    }

    override suspend fun getSetlistById(id: Long): Setlist? {
        TODO("Not yet implemented")
    }
}

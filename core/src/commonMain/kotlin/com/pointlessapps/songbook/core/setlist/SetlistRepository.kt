package com.pointlessapps.songbook.core.setlist

import com.pointlessapps.songbook.core.database.dao.SetlistDao
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistWithCount
import com.pointlessapps.songbook.core.setlist.database.mapper.toDomain
import com.pointlessapps.songbook.core.setlist.database.mapper.toEntities
import com.pointlessapps.songbook.core.setlist.model.NewSetlist
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.mapper.toDomain
import com.pointlessapps.songbook.core.song.model.Song
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface SetlistRepository {
    fun getAllSetlistsFlow(limit: Long = -1L): Flow<List<Setlist>>
    fun getSetlistByIdFlow(id: Long): Flow<Setlist?>
    fun getSetlistsSongsById(id: Long): Flow<List<Song>>

    suspend fun addSetlist(name: String): Long
    suspend fun deleteSetlist(id: Long)
    suspend fun updateSetlistName(id: Long, name: String)
    suspend fun updateSetlistSongsOrder(id: Long, songs: List<Song>)
}

@OptIn(SupabaseExperimental::class)
internal class SetlistRepositoryImpl(
    supabase: SupabaseClient,
    private val setlistDao: SetlistDao,
) : SetlistRepository {

    private val setlistsTable = supabase.from("setlists")
    private val setlistSongsTable = supabase.from("setlist_songs")

    override fun getAllSetlistsFlow(limit: Long): Flow<List<Setlist>> =
        setlistDao.getAllSetlistsFlow(limit)
            .map { it.map(SetlistWithCount::toDomain) }
            .flowOn(Dispatchers.IO)

    override fun getSetlistByIdFlow(id: Long) = setlistDao.getSetlistByIdFlow(id)
        .map { it?.toDomain() }
        .flowOn(Dispatchers.IO)

    override fun getSetlistsSongsById(id: Long) = setlistDao.getSetlistSongsById(id)
        .map { it.map(SongEntity::toDomain) }
        .flowOn(Dispatchers.IO)

    override suspend fun addSetlist(name: String) = withContext(Dispatchers.IO) {
        setlistsTable.upsert(NewSetlist(name)) { select() }
            .decodeSingle<SetlistEntity>().id.also {
                setlistDao.insertSetlists(listOf(SetlistEntity(it, name)))
            }
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

package com.pointlessapps.songbook.core.setlist

import com.pointlessapps.songbook.core.database.dao.SetlistDao
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistWithCount
import com.pointlessapps.songbook.core.setlist.database.mapper.toDomain
import com.pointlessapps.songbook.core.setlist.database.mapper.toEntity
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.mapper.toDomain
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.sync.database.dao.SyncActionDao
import com.pointlessapps.songbook.core.sync.database.entity.SyncAction
import com.pointlessapps.songbook.core.sync.database.entity.SyncActionEntity
import io.github.jan.supabase.annotations.SupabaseExperimental
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface SetlistRepository {
    fun getAllSetlistsFlow(limit: Long = -1L): Flow<List<Setlist>>
    fun getSetlistByIdFlow(id: String): Flow<Setlist?>
    fun getSetlistsSongsById(id: String): Flow<List<Song>>

    suspend fun addSetlist(name: String): String
    suspend fun deleteSetlist(id: String)
    suspend fun updateSetlistName(id: String, name: String)
    suspend fun updateSetlistSongsOrder(id: String, songsIds: List<String>)
    suspend fun addSongToSetlist(setlistId: String, songId: String, order: Int)
    suspend fun removeSongFromSetlist(setlistId: String, songId: String)
}

@OptIn(SupabaseExperimental::class, ExperimentalUuidApi::class)
internal class SetlistRepositoryImpl(
    private val setlistDao: SetlistDao,
    private val syncActionDao: SyncActionDao,
) : SetlistRepository {

    override fun getAllSetlistsFlow(limit: Long): Flow<List<Setlist>> =
        setlistDao.getAllSetlistsFlow(limit)
            .map { it.map(SetlistWithCount::toDomain) }
            .flowOn(Dispatchers.IO)

    override fun getSetlistByIdFlow(id: String) = setlistDao.getSetlistByIdFlow(id)
        .map { it?.toDomain() }
        .flowOn(Dispatchers.IO)

    override fun getSetlistsSongsById(id: String) = setlistDao.getSetlistSongsById(id)
        .map { it.map(SongEntity::toDomain) }
        .flowOn(Dispatchers.IO)

    override suspend fun addSetlist(name: String) = withContext(Dispatchers.IO) {
        val id = Uuid.random().toString()
        val setlist = Setlist(id, name)

        setlistDao.insertSetlists(listOf(setlist.toEntity()))

        syncActionDao.insertAction(
            SyncActionEntity(
                syncAction = SyncAction.AddSetlist(setlist),
            ),
        )

        return@withContext id
    }

    override suspend fun deleteSetlist(id: String) {
        withContext(Dispatchers.IO) {
            setlistDao.deleteSetlist(id)

            syncActionDao.insertAction(
                SyncActionEntity(
                    syncAction = SyncAction.DeleteSetlist(id),
                ),
            )
        }
    }

    override suspend fun updateSetlistName(id: String, name: String) {
        withContext(Dispatchers.IO) {
            setlistDao.updateSetlistName(id, name)

            syncActionDao.insertAction(
                SyncActionEntity(
                    syncAction = SyncAction.UpdateSetlistName(id, name),
                ),
            )
        }
    }

    override suspend fun updateSetlistSongsOrder(id: String, songsIds: List<String>) {
        withContext(Dispatchers.IO) {
            setlistDao.insertSetlistSongs(
                songsIds.mapIndexed { index, songId ->
                    SetlistSongEntity(id, songId, index)
                },
            )

            syncActionDao.insertAction(
                SyncActionEntity(
                    syncAction = SyncAction.UpdateSetlistSongsOrder(id, songsIds),
                ),
            )
        }
    }

    override suspend fun addSongToSetlist(setlistId: String, songId: String, order: Int) {
        withContext(Dispatchers.IO) {
            val entity = SetlistSongEntity(setlistId, songId, order)
            setlistDao.insertSetlistSongs(listOf(entity))

            syncActionDao.insertAction(
                SyncActionEntity(
                    syncAction = SyncAction.AddSongToSetlist(entity),
                ),
            )
        }
    }

    override suspend fun removeSongFromSetlist(setlistId: String, songId: String) {
        withContext(Dispatchers.IO) {
            setlistDao.deleteSetlistSong(setlistId, songId)

            syncActionDao.insertAction(
                SyncActionEntity(
                    syncAction = SyncAction.RemoveSongFromSetlist(setlistId, songId),
                ),
            )
        }
    }
}

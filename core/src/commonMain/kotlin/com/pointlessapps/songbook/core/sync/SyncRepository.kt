package com.pointlessapps.songbook.core.sync

import com.pointlessapps.songbook.core.database.dao.SetlistDao
import com.pointlessapps.songbook.core.database.dao.SongDao
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.mapper.toEntity
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.database.mapper.toEntity
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresListDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart

interface SyncRepository {
    val currentSyncStatus: StateFlow<SyncStatus>

    suspend fun startSync()
    suspend fun stopSync()
}

internal class SyncRepositoryImpl(
    supabase: SupabaseClient,
    private val songDao: SongDao,
    private val setlistDao: SetlistDao,
) : SyncRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _syncStatus = MutableStateFlow(SyncStatus.LOCAL)
    override val currentSyncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val channel = supabase.channel(SYNC_CHANNEL_ID)

    override suspend fun startSync() {
        combine(
            channel.postgresListDataFlow(table = SONGS_TABLE, primaryKey = Song::id),
            channel.postgresListDataFlow(table = SETLISTS_TABLE, primaryKey = Setlist::id),
            channel.postgresListDataFlow(
                table = SETLIST_SONGS_TABLE,
                primaryKeys = listOf(
                    SetlistSongEntity::setlistId,
                    SetlistSongEntity::songId,
                ),
            ),
        ) { songs, setlists, setlistSongs ->
            _syncStatus.value = SyncStatus.SYNCING

            saveSongs(songs)
            saveSetlists(setlists)
            saveSetlistSongs(setlistSongs)

            _syncStatus.value = SyncStatus.SYNCED
        }.onStart {
            _syncStatus.value = SyncStatus.SYNCING
        }.catch {
            _syncStatus.value = SyncStatus.SYNC_FAILED
        }.launchIn(coroutineScope)

        channel.subscribe()
    }

    override suspend fun stopSync() {
        channel.unsubscribe()
        _syncStatus.value = SyncStatus.LOCAL
    }

    private suspend fun saveSongs(songs: List<Song>) {
        songDao.insertSongsWithSearch(songs.map(Song::toEntity))
    }

    private suspend fun saveSetlists(setlists: List<Setlist>) {
        setlistDao.insertSetlists(setlists.map(Setlist::toEntity))
    }

    private suspend fun saveSetlistSongs(setlistSongs: List<SetlistSongEntity>) {
        setlistDao.insertSetlistSongs(setlistSongs)
    }

    private companion object {
        const val SONGS_TABLE = "songs"
        const val SETLISTS_TABLE = "setlists"
        const val SETLIST_SONGS_TABLE = "setlist_songs"

        const val SYNC_CHANNEL_ID = "sync_channel"
    }
}

package com.pointlessapps.songbook.core.sync

import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.database.dao.SetlistDao
import com.pointlessapps.songbook.core.database.dao.SongDao
import com.pointlessapps.songbook.core.network.NetworkRepository
import com.pointlessapps.songbook.core.network.model.NetworkStatus
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.mapper.toEntity
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.database.mapper.toEntity
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.sync.database.dao.SyncActionDao
import com.pointlessapps.songbook.core.sync.database.entity.SyncAction
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresListDataFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

interface SyncRepository {
    val currentSyncStatus: StateFlow<SyncStatus>

    fun observeRemoteAsFlow(): Flow<Unit>
    suspend fun sync(): Result<Unit>
}

internal class SyncRepositoryImpl(
    supabase: SupabaseClient,
    private val songDao: SongDao,
    private val setlistDao: SetlistDao,
    private val syncActionDao: SyncActionDao,
    private val authRepository: AuthRepository,
    private val networkRepository: NetworkRepository,
) : SyncRepository {

    private val songsTable = supabase.from(SONGS_TABLE)
    private val setlistsTable = supabase.from(SETLISTS_TABLE)
    private val setlistSongsTable = supabase.from(SETLIST_SONGS_TABLE)

    private val _syncStatus = MutableStateFlow(SyncStatus.LOCAL)
    override val currentSyncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val channel = supabase.channel(SYNC_CHANNEL_ID)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRemoteAsFlow() = networkRepository.networkStatus.flatMapLatest { status ->
        if (status == NetworkStatus.OFFLINE) {
            _syncStatus.value = SyncStatus.OFFLINE
            return@flatMapLatest flowOf(Unit)
        }

        flow {
            authRepository.initialize()
            if (!authRepository.isSignedIn()) {
                authRepository.signInAnonymously()
            }
            emit(Unit)
        }.flatMapLatest {
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
            }
        }.onStart {
            _syncStatus.value = SyncStatus.SYNCING
        }.onCompletion {
            it?.printStackTrace()
            _syncStatus.value = if (it != null) SyncStatus.SYNC_FAILED else SyncStatus.LOCAL
        }
    }

    override suspend fun sync(): Result<Unit> {
        networkRepository.networkStatus.firstOrNull()?.let {
            if (it == NetworkStatus.OFFLINE) {
                _syncStatus.value = SyncStatus.OFFLINE

                return Result.success(Unit)
            }
        }

        val actions = syncActionDao.getAllActions()
        if (actions.isEmpty()) return Result.success(Unit)

        return runCatching {
            authRepository.initialize()
            if (!authRepository.isSignedIn()) {
                authRepository.signInAnonymously()
            }

            _syncStatus.value = SyncStatus.SYNCING

            actions.forEach { action ->
                when (val payload = action.syncAction) {
                    is SyncAction.SaveSong -> songsTable.upsert(payload.song)

                    is SyncAction.DeleteSong ->
                        songsTable.delete { filter { Song::id eq payload.id } }

                    is SyncAction.AddSetlist -> setlistsTable.upsert(payload.setlist)

                    is SyncAction.DeleteSetlist ->
                        setlistsTable.delete { filter { Setlist::id eq payload.id } }

                    is SyncAction.UpdateSetlistName ->
                        setlistsTable.update(
                            update = { Setlist::name setTo payload.name },
                            request = { filter { Setlist::id eq payload.id } },
                        )

                    is SyncAction.UpdateSongSetlists -> {
                        setlistSongsTable.delete { filter { SetlistSongEntity::songId eq payload.id } }
                        setlistSongsTable.upsert(
                            payload.setlistsIds.mapIndexed { index, setlistId ->
                                SetlistSongEntity(setlistId, payload.id, index)
                            },
                        )
                    }

                    is SyncAction.UpdateSetlistSongs -> {
                        setlistSongsTable.delete { filter { SetlistSongEntity::setlistId eq payload.id } }
                        setlistSongsTable.upsert(
                            payload.songsIds.mapIndexed { index, songId ->
                                SetlistSongEntity(payload.id, songId, index)
                            },
                        )
                    }

                    is SyncAction.AddSongToSetlist ->
                        setlistSongsTable.upsert(payload.setlistSongEntity)

                    is SyncAction.RemoveSongFromSetlist ->
                        setlistSongsTable.delete {
                            filter {
                                SetlistSongEntity::setlistId eq payload.setlistId
                                and { SetlistSongEntity::songId eq payload.songId }
                            }
                        }
                }

                syncActionDao.deleteAction(action.id)
            }

            _syncStatus.value = SyncStatus.SYNCED
        }.onFailure {
            it.printStackTrace()
            _syncStatus.value = SyncStatus.SYNC_FAILED
        }
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

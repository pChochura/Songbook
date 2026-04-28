package com.pointlessapps.songbook.core.sync

import com.pointlessapps.songbook.core.database.dao.SyncDao
import com.pointlessapps.songbook.core.network.NetworkRepository
import com.pointlessapps.songbook.core.network.model.NetworkStatus
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.mapper.toEntity
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.database.mapper.toEntity
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.sync.database.dao.SyncActionDao
import com.pointlessapps.songbook.core.sync.database.entity.SyncAction
import com.pointlessapps.songbook.core.sync.database.entity.SyncActionEntity
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds

interface SyncRepository {
    val currentSyncStatusFlow: StateFlow<SyncStatus>

    fun performSyncAsFlow(): Flow<Unit>

    suspend fun clearDatabase()
}

internal class SyncRepositoryImpl(
    supabase: SupabaseClient,
    private val syncDao: SyncDao,
    private val syncActionDao: SyncActionDao,
    private val networkRepository: NetworkRepository,
) : SyncRepository {

    private val songsTable = supabase.from(SONGS_TABLE)
    private val setlistsTable = supabase.from(SETLISTS_TABLE)
    private val setlistSongsTable = supabase.from(SETLIST_SONGS_TABLE)

    private val _syncStatus = MutableStateFlow(SyncStatus.LOCAL)
    override val currentSyncStatusFlow: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val syncMutex = Mutex()

    @OptIn(ExperimentalCoroutinesApi::class, SupabaseExperimental::class, FlowPreview::class)
    override fun performSyncAsFlow() = networkRepository.networkStatus.flatMapLatest { status ->
        if (status == NetworkStatus.OFFLINE) {
            _syncStatus.value = SyncStatus.OFFLINE
            return@flatMapLatest flowOf(Unit)
        }

        combine(
            syncActionDao.getAllActionsFlow(),
            songsTable.selectAsFlow(primaryKey = Song::id),
            setlistsTable.selectAsFlow(primaryKey = Setlist::id),
            setlistSongsTable.selectAsFlow(
                primaryKeys = listOf(
                    SetlistSongEntity::setlistId,
                    SetlistSongEntity::songId,
                ),
            ),
        ) { syncActions, songs, setlists, setlistSongs ->
            SyncPayload(
                actions = syncActions,
                songs = songs,
                setlists = setlists,
                setlistSongs = setlistSongs,
            )
        }.debounce(SYNC_DEBOUNCE).conflate().map {
            syncMutex.withLock { performSafeSync(it) }
        }.onCompletion {
            it?.printStackTrace()
            _syncStatus.value = if (it != null) SyncStatus.SYNC_FAILED else SyncStatus.LOCAL
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun performSafeSync(payload: SyncPayload) {
        _syncStatus.value = SyncStatus.SYNCING

        if (payload.actions.isNotEmpty()) {
            processRemoteActions(payload.actions)
        } else {
            saveData(
                songs = payload.songs,
                setlists = payload.setlists,
                setlistSongs = payload.setlistSongs,
            )
        }

        _syncStatus.value = SyncStatus.SYNCED
    }

    private suspend fun processRemoteActions(actions: List<SyncActionEntity>) {
        val finishedActionIds = mutableSetOf<Long>()

        val exception = runCatching {
            actions.forEach { action ->
                when (val payload = action.syncAction) {
                    is SyncAction.SaveSong -> {
                        songsTable.upsert(payload.song)
                        setlistSongsTable.upsert(
                            payload.setlistsIds.mapIndexed { index, setlistId ->
                                SetlistSongEntity(setlistId, payload.song.id, index)
                            },
                        )
                    }

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
                        setlistSongsTable.delete {
                            filter {
                                SetlistSongEntity::songId eq payload.id
                                and(negate = true) {
                                    SetlistSongEntity::setlistId isIn payload.setlistsIds
                                }
                            }
                        }
                        setlistSongsTable.upsert(
                            payload.setlistsIds.mapIndexed { index, setlistId ->
                                SetlistSongEntity(setlistId, payload.id, index)
                            },
                        )
                    }

                    is SyncAction.UpdateSetlistSongs -> {
                        setlistSongsTable.delete {
                            filter {
                                SetlistSongEntity::setlistId eq payload.id
                                and(negate = true) {
                                    SetlistSongEntity::songId isIn payload.songsIds
                                }
                            }
                        }
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

                finishedActionIds.add(action.id)
            }
        }.exceptionOrNull()

        syncActionDao.deleteActions(finishedActionIds.toList())
        exception?.let { throw it }
    }

    private suspend fun saveData(
        songs: List<Song>,
        setlists: List<Setlist>,
        setlistSongs: List<SetlistSongEntity>,
    ) = syncDao.replaceAll(
        songs = songs.map(Song::toEntity),
        setlists = setlists.map(Setlist::toEntity),
        setlistSongs = setlistSongs,
    )

    override suspend fun clearDatabase() = syncDao.clear()

    private data class SyncPayload(
        val actions: List<SyncActionEntity>,
        val songs: List<Song>,
        val setlists: List<Setlist>,
        val setlistSongs: List<SetlistSongEntity>,
    )

    private companion object {
        const val SONGS_TABLE = "songs"
        const val SETLISTS_TABLE = "setlists"
        const val SETLIST_SONGS_TABLE = "setlist_songs"

        val SYNC_DEBOUNCE = 2.seconds
    }
}

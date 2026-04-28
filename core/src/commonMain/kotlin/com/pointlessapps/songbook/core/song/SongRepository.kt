package com.pointlessapps.songbook.core.song

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.pointlessapps.songbook.core.database.dao.SongDao
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.mapper.toDomain
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchResult
import com.pointlessapps.songbook.core.song.database.mapper.toDomain
import com.pointlessapps.songbook.core.song.database.mapper.toEntity
import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.sync.database.dao.SyncActionDao
import com.pointlessapps.songbook.core.sync.database.entity.SyncAction
import com.pointlessapps.songbook.core.sync.database.entity.SyncActionEntity
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface SongRepository {
    fun getAllSongs(): Flow<PagingData<Song>>
    fun getSongByIdFlow(id: String): Flow<Song?>
    fun searchSongs(query: String): Flow<PagingData<SongSearchResult>>
    fun getSongSetlistsById(id: String): Flow<ImmutableList<Setlist>>

    suspend fun updateSongSetlists(id: String, setlistsIds: List<String>)
    suspend fun saveSong(newSong: NewSong, setlistsIds: List<String>): String
    suspend fun deleteSong(id: String)
}

@OptIn(ExperimentalUuidApi::class)
internal class SongRepositoryImpl(
    private val songDao: SongDao,
    private val syncActionDao: SyncActionDao,
) : SongRepository {

    override fun getAllSongs() = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { songDao.getAllSongs() },
    ).flow.map { pagingData ->
        pagingData.map(SongEntity::toDomain)
    }.flowOn(Dispatchers.Default)

    override fun getSongByIdFlow(id: String) = songDao.getSongByIdFlow(id)
        .map { it?.toDomain() }
        .flowOn(Dispatchers.IO)

    override fun searchSongs(query: String): Flow<PagingData<SongSearchResult>> {
        if (query.isBlank()) return flowOf(PagingData.empty())

        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { songDao.searchSongs("$query*") },
        ).flow.flowOn(Dispatchers.IO)
    }

    override fun getSongSetlistsById(id: String) = songDao.getSongSetlistsById(id)
        .map { it.map(SetlistEntity::toDomain).toImmutableList() }
        .flowOn(Dispatchers.IO)

    override suspend fun updateSongSetlists(id: String, setlistsIds: List<String>) {
        withContext(Dispatchers.IO) {
            songDao.updateSongSetlists(
                songId = id,
                setlistSongs = setlistsIds.mapIndexed { index, setlistId ->
                    SetlistSongEntity(setlistId, id, index)
                },
            )

            syncActionDao.insertAction(
                SyncActionEntity(
                    syncAction = SyncAction.UpdateSongSetlists(id, setlistsIds),
                ),
            )
        }
    }

    override suspend fun saveSong(newSong: NewSong, setlistsIds: List<String>) =
        withContext(Dispatchers.IO) {
            val song = Song(
                id = newSong.id ?: Uuid.random().toString(),
                title = newSong.title,
                artist = newSong.artist,
                lyrics = newSong.lyrics,
            )

            songDao.insertSongWithSetlists(
                song = song.toEntity(),
                setlistsSongs = setlistsIds.mapIndexed { index, setlistId ->
                    SetlistSongEntity(setlistId, song.id, index)
                },
            )

            syncActionDao.insertAction(
                SyncActionEntity(
                    syncAction = SyncAction.SaveSong(song, setlistsIds),
                ),
            )

            return@withContext song.id
        }

    override suspend fun deleteSong(id: String) {
        withContext(Dispatchers.IO) {
            songDao.deleteSongWithSearch(id)

            syncActionDao.insertAction(
                SyncActionEntity(
                    syncAction = SyncAction.DeleteSong(id),
                ),
            )
        }
    }
}

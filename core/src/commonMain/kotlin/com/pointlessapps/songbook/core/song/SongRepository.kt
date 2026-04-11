package com.pointlessapps.songbook.core.song

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.pointlessapps.songbook.core.database.dao.SongDao
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchResult
import com.pointlessapps.songbook.core.song.database.mapper.toDomain
import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Song
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface SongRepository {
    fun getAllSongs(): Flow<PagingData<Song>>
    fun getSongByIdFlow(id: Long): Flow<Song?>
    fun searchSongs(query: String): Flow<PagingData<SongSearchResult>>

    suspend fun saveSong(newSong: NewSong): Long
    suspend fun deleteSong(id: Long)
}

@OptIn(SupabaseExperimental::class)
internal class SongRepositoryImpl(
    supabase: SupabaseClient,
    private val songDao: SongDao,
) : SongRepository {

    private val table = supabase.from("songs")

    override fun getAllSongs() = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { songDao.getAllSongs() },
    ).flow.map { pagingData ->
        pagingData.map(SongEntity::toDomain)
    }.flowOn(Dispatchers.IO)

    override fun getSongByIdFlow(id: Long) = songDao.getSongByIdFlow(id)
        .map { it?.toDomain() }
        .flowOn(Dispatchers.IO)

    override fun searchSongs(query: String): Flow<PagingData<SongSearchResult>> {
        if (query.isBlank()) return flowOf(PagingData.empty())

        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { songDao.searchSongs("$query*") },
        ).flow.flowOn(Dispatchers.IO)
    }

    override suspend fun saveSong(newSong: NewSong) = withContext(Dispatchers.IO) {
        table.upsert(newSong) { select() }.decodeSingle<Song>().id
    }

    override suspend fun deleteSong(id: Long) {
        // TODO mark as deleted instead of removing
        withContext(Dispatchers.IO) {
            table.delete { filter { Song::id eq id } }
            songDao.deleteSongWithSearch(id)
        }
    }
}

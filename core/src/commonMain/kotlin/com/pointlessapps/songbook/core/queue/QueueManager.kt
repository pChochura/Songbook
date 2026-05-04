package com.pointlessapps.songbook.core.queue

import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.utils.emptyImmutableList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

interface QueueManager {
    val queueFlow: StateFlow<ImmutableList<Song>>
    val currentSongFlow: StateFlow<Song?>

    suspend fun setSong(songId: String)
    suspend fun setQueue(songsIds: List<String>, currentSongId: String)

    fun goToNextSong()
    fun goToPreviousSong()

    suspend fun peekPreviousSong(): Song?
    suspend fun peekNextSong(): Song?
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class QueueManagerImpl(
    private val songRepository: SongRepository,
) : QueueManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _queueFlow = MutableStateFlow<ImmutableList<String>>(emptyImmutableList())
    override val queueFlow = _queueFlow
        .flatMapLatest(songRepository::getSongsByIdFlow)
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyImmutableList(),
        )

    private val _currentSongIndexFlow = MutableStateFlow(-1)
    override val currentSongFlow: StateFlow<Song?> = combine(
        _queueFlow,
        _currentSongIndexFlow,
    ) { queue, index -> queue.getOrNull(index) }.flatMapLatest {
        it?.let(songRepository::getSongByIdFlow) ?: emptyFlow()
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    override suspend fun setSong(songId: String) = setQueue(listOf(songId), songId)
    override suspend fun setQueue(songsIds: List<String>, currentSongId: String) {
        _queueFlow.value = songsIds.toImmutableList()
        _currentSongIndexFlow.value = songsIds.indexOf(currentSongId)
    }

    override fun goToNextSong() {
        if (_currentSongIndexFlow.value < _queueFlow.value.size - 1) {
            _currentSongIndexFlow.value++
        }
    }

    override fun goToPreviousSong() {
        if (_currentSongIndexFlow.value > 0) {
            _currentSongIndexFlow.value--
        }
    }

    override suspend fun peekPreviousSong(): Song? =
        _queueFlow.value.getOrNull(_currentSongIndexFlow.value - 1)
            ?.let(songRepository::getSongByIdFlow)
            ?.firstOrNull()

    override suspend fun peekNextSong(): Song? =
        _queueFlow.value.getOrNull(_currentSongIndexFlow.value + 1)
            ?.let(songRepository::getSongByIdFlow)
            ?.firstOrNull()
}

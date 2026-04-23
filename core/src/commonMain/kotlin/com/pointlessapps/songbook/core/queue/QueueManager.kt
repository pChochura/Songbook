package com.pointlessapps.songbook.core.queue

import com.pointlessapps.songbook.core.song.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

interface QueueManager {
    val queueFlow: StateFlow<List<Song>>
    val currentSongFlow: StateFlow<Song?>

    suspend fun setQueue(songs: List<Song>, currentSong: Song)

    fun goToNextSong()
    fun goToPreviousSong()

    fun peekPreviousSong(): Song?
    fun peekNextSong(): Song?
}

internal class QueueManagerImpl : QueueManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _queueFlow = MutableStateFlow<List<Song>>(emptyList())
    override val queueFlow = _queueFlow.asStateFlow()

    private val _currentSongIndexFlow = MutableStateFlow(-1)
    override val currentSongFlow: StateFlow<Song?> = combine(
        _queueFlow,
        _currentSongIndexFlow,
    ) { queue, index -> queue.getOrNull(index) }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    override suspend fun setQueue(songs: List<Song>, currentSong: Song) {
        _queueFlow.value = songs
        _currentSongIndexFlow.value = songs.indexOf(currentSong)
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

    override fun peekPreviousSong(): Song? =
        _queueFlow.value.getOrNull(_currentSongIndexFlow.value - 1)

    override fun peekNextSong(): Song? =
        _queueFlow.value.getOrNull(_currentSongIndexFlow.value + 1)
}

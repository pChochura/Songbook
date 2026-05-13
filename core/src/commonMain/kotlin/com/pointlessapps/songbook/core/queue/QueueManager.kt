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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

interface QueueManager {
    val queueFlow: StateFlow<ImmutableList<Song>>
    val currentSongFlow: StateFlow<Song?>
    val previousSongFlow: StateFlow<Song?>
    val nextSongFlow: StateFlow<Song?>

    fun setSong(songId: String)
    fun clearQueueAndSetSong(songId: String)
    fun setQueue(songsIds: List<String>, currentSongId: String)
    fun clearQueue()
    fun removeFromQueue(songId: String)
    fun removeFromQueue(songsIds: List<String>)
    fun updateOrder(songsIds: List<String>)

    fun goToNextSong(): Boolean
    fun goToPreviousSong(): Boolean
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

    private val _currentSongIdFlow = MutableStateFlow<String?>(null)
    override val currentSongFlow: StateFlow<Song?> = _currentSongIdFlow
        .flatMapLatest {
            it?.let(songRepository::getSongByIdFlow) ?: flowOf(null)
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    override val previousSongFlow: StateFlow<Song?> = combine(
        _currentSongIdFlow,
        _queueFlow,
    ) { currentSongId, queue ->
        currentSongId to queue
    }.flatMapLatest { (currentSongId, queue) ->
        val songIndex = indexOf(currentSongId ?: return@flatMapLatest flowOf(null))
        val songId = queue.getOrNull(songIndex - 1)
        songId?.let(songRepository::getSongByIdFlow) ?: flowOf(null)
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    override val nextSongFlow: StateFlow<Song?> = combine(
        _currentSongIdFlow,
        _queueFlow,
    ) { currentSongId, queue ->
        currentSongId to queue
    }.flatMapLatest { (currentSongId, queue) ->
        val songIndex = indexOf(currentSongId ?: return@flatMapLatest flowOf(null))
        val songId = queue.getOrNull(songIndex + 1)
        songId?.let(songRepository::getSongByIdFlow) ?: flowOf(null)
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    override fun setSong(songId: String) {
        _currentSongIdFlow.value = songId
    }

    override fun clearQueueAndSetSong(songId: String) = setQueue(listOf(songId), songId)

    override fun setQueue(songsIds: List<String>, currentSongId: String) {
        _queueFlow.value = songsIds.toImmutableList()
        _currentSongIdFlow.value = currentSongId
    }

    override fun clearQueue() {
        _queueFlow.value = emptyImmutableList()
        _currentSongIdFlow.value = null
    }

    override fun removeFromQueue(songId: String) {
        _queueFlow.value = _queueFlow.value.filterNot { it == songId }.toImmutableList()

        if (_currentSongIdFlow.value == songId && !goToNextSong()) {
            _currentSongIdFlow.value = null
        }
    }

    override fun removeFromQueue(songsIds: List<String>) {
        _queueFlow.value = _queueFlow.value.filterNot { it in songsIds }.toImmutableList()

        if (songsIds.contains(_currentSongIdFlow.value) && !goToNextSong()) {
            _currentSongIdFlow.value = null
        }
    }

    override fun updateOrder(songsIds: List<String>) {
        _queueFlow.value = songsIds.toImmutableList()
    }

    override fun goToNextSong(): Boolean {
        val songId = _currentSongIdFlow.value ?: return false
        val songIndex = indexOf(songId)

        if (songIndex < _queueFlow.value.size - 1) {
            _currentSongIdFlow.value = _queueFlow.value[songIndex + 1]

            return true
        }

        return false
    }

    override fun goToPreviousSong(): Boolean {
        val songId = _currentSongIdFlow.value ?: return false
        val songIndex = indexOf(songId)

        if (songIndex > 0) {
            _currentSongIdFlow.value = _queueFlow.value[songIndex - 1]

            return true
        }

        return false
    }

    private fun indexOf(songId: String) = _queueFlow.value.indexOf(songId)
}

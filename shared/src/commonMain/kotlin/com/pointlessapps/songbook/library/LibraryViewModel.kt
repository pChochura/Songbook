package com.pointlessapps.songbook.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.data.SongDao
import com.pointlessapps.songbook.data.SongEntity
import com.pointlessapps.songbook.ui.components.NavigationDestination
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal data class LibraryState(
    val selectedDestination: NavigationDestination = NavigationDestination.Library,
    val songs: List<SongEntity> = emptyList(),
    val isLoading: Boolean = false,
    val totalSongs: Int = 0,
    val totalArtists: Int = 0,
)

internal class LibraryViewModel(
    private val songDao: SongDao,
) : ViewModel() {

    var state by mutableStateOf(LibraryState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            songDao.getAllSongs().collectLatest { songs ->
                val finalSongs = if (songs.isEmpty()) getMockSongs() else songs
                state = state.copy(
                    songs = finalSongs,
                    totalSongs = finalSongs.size,
                    totalArtists = finalSongs.distinctBy { it.artist }.size,
                    isLoading = false,
                )
            }
        }
    }

    private fun getMockSongs() = listOf(
        SongEntity(1, "Wonderwall", "Oasis", "Today is gonna be the day", "G Major", "4:18", 87),
        SongEntity(2, "Hotel California", "Eagles", "On a dark desert highway", "B Minor", "6:30", 74),
        SongEntity(3, "10,000 Reasons", "Matt Redman", "Bless the Lord oh my soul", "G Major", "5:42", 73),
        SongEntity(4, "Fly Me To The Moon", "Frank Sinatra", "Fly me to the moon", "C Major", "2:27", 119),
        SongEntity(5, "Slow Dancing in a Burning Room", "John Mayer", "It's not a silly little moment", "C# Minor", "4:02", 67),
        SongEntity(6, "Bohemian Rhapsody", "Queen", "Is this the real life?", "Bb Major", "5:55", 72),
    )

    fun onDestinationSelected(destination: NavigationDestination) {
        state = state.copy(selectedDestination = destination)
    }
}

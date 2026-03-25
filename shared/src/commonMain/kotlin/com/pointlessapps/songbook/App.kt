package com.pointlessapps.songbook

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.pointlessapps.songbook.ui.components.LocalSnackbarHostState
import com.pointlessapps.songbook.ui.components.rememberSongbookSnackbarHostState
import com.pointlessapps.songbook.ui.theme.SongbookTheme

@Composable
fun App(
    initialFilterLetter: String? = null,
    openSearch: Boolean = false,
) {
    val songbookSnackbarHostState = rememberSongbookSnackbarHostState()

    SongbookTheme {
        CompositionLocalProvider(
            LocalSnackbarHostState provides songbookSnackbarHostState,
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            ),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Navigator(
                    startingRoute = Route.Library(
                        initialFilterLetter = initialFilterLetter,
                        openSearch = openSearch,
                    ),
                )
            }
        }
    }
}

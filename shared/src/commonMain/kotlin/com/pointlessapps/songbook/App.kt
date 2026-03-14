package com.pointlessapps.songbook

import androidx.compose.runtime.Composable
import com.pointlessapps.songbook.ui.theme.LyricFlowTheme

@Composable
fun App(
    initialFilterLetter: String? = null,
    openSearch: Boolean = false,
) {
    LyricFlowTheme {
        Navigator(startingRoute = Route.Library)
    }
}

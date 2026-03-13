package com.pointlessapps.songbook

import androidx.compose.runtime.Composable
import com.pointlessapps.songbook.ui.theme.LyricFlowTheme

@Composable
fun App() {
    LyricFlowTheme {
        Navigator(startingRoute = Route.Library)
    }
}

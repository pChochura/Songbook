package com.pointlessapps.songbook.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.zIndex

@Composable
fun SongbookLoader(
    enabled: Boolean,
    scrimAlpha: Float = SCRIM_ALPHA,
) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(enabled) {
        if (enabled) {
            focusManager.clearFocus(true)
        }
    }

    AnimatedVisibility(
        modifier = Modifier.zIndex(1f),
        visible = enabled,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = scrimAlpha)) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private const val SCRIM_ALPHA = 0.7f

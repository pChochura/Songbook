package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongbookBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    var currentlyShown by remember(Unit) { mutableStateOf(show) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(show) {
        if (show) {
            currentlyShown = true
        } else {
            sheetState.hide()
            currentlyShown = false
        }
    }

    if (currentlyShown) {
        ModalBottomSheet(
            contentWindowInsets = { WindowInsets() },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
            dragHandle = null,
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            content = content,
        )
    }
}

package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout

@Composable
fun SongbookScaffoldLayout(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val topBarPlaceables = subcompose(SongbookScaffoldLayoutContent.TopBar, topBar).map {
            it.measure(looseConstraints)
        }

        val topBarHeight = topBarPlaceables.maxByOrNull { it.height }?.height ?: 0

        val fabPlaceables = subcompose(SongbookScaffoldLayoutContent.Fab, fab)
            .mapNotNull { measurable ->
                measurable.measure(looseConstraints).takeIf { it.height != 0 && it.width != 0 }
            }

        val fabHeight = fabPlaceables.maxByOrNull { it.height }?.height ?: 0

        val bodyContentPlaceables = subcompose(SongbookScaffoldLayoutContent.Content) {
            content(
                PaddingValues(
                    top = topBarHeight.toDp(),
                    bottom = fabHeight.toDp(),
                ),
            )
        }.map { it.measure(looseConstraints.copy(maxHeight = layoutHeight)) }

        layout(layoutWidth, layoutHeight) {
            bodyContentPlaceables.forEach { it.place(0, 0) }
            topBarPlaceables.forEach { it.place(0, 0) }
            fabPlaceables.forEach { fab ->
                fab.place(0, layoutHeight - fabHeight)
            }
        }
    }
}

private enum class SongbookScaffoldLayoutContent { TopBar, Content, Fab }

package com.pointlessapps.songbook.lyrics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.lyrics.LyricsViewModel
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.lyrics_section_label
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconArrowLeft
import com.pointlessapps.songbook.ui.theme.spacing

@Composable
internal fun LyricsScreen(
    viewModel: LyricsViewModel,
) {
    val state = viewModel.state
    val navigator = LocalNavigator.current

    SongbookScaffoldLayout(
        topBar = @Composable {
            TopBar(
                leftButton = TopBarButton(
                    icon = IconArrowLeft,
                    tooltip = Res.string.common_back,
                    onClick = { navigator.navigateBack() },
                ),
                rightButton = null,
                title = Res.string.lyrics_section_label,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp)
                .padding(horizontal = MaterialTheme.spacing.huge),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(
                space = MaterialTheme.spacing.small,
                alignment = Alignment.Top,
            ),
        ) {
            item(key = "header") {
                SongHeader(
                    title = state.title,
                    artist = state.artist,
                )
            }

            item(key = "lyrics") {

            }
            items(state.parsedSections) {
                LyricsSection() {

                }
            }
        }
    }
}

@Composable
private fun SongHeader(title: String, artist: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.medium,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        SongbookText(
            text = title,
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurface,
                typography = MaterialTheme.typography.titleLarge,
            ),
        )
        SongbookText(
            text = artist,
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                typography = MaterialTheme.typography.labelMedium,
            ),
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

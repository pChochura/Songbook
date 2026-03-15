package com.pointlessapps.songbook.library.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.library.ImportSongEvent
import com.pointlessapps.songbook.library.ImportSongViewModel
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_artist_label
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_artist_placeholder
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_cancel
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_confirm
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_header_subtitle
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_header_title
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_lyrics_label
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_lyrics_placeholder
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_lyrics_tip
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_ocr_button
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_song_title_label
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_song_title_placeholder
import com.pointlessapps.songbook.ui.theme.spacing
import com.preat.peekaboo.image.picker.FilterOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ImportSongScreen(
    viewModel: ImportSongViewModel,
) {
    val state = viewModel.state
    val navigator = LocalNavigator.current
    val imagePickerLauncher = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = rememberCoroutineScope(),
        filterOptions = FilterOptions.GrayScale,
        onResult = { viewModel.onImageCaptured(it.firstOrNull()) },
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ImportSongEvent.Back -> navigator.navigateToLibrary()
                is ImportSongEvent.NavigateToLyrics -> navigator.navigateToLyrics(event.songId)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                IconButton(onClick = { viewModel.onBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
                Text(
                    text = stringResource(Res.string.import_dialog_header_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    ) { paddingValues ->
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colorScheme.primary,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(MaterialTheme.spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            // Header Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape,
                        )
                        .padding(MaterialTheme.spacing.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(MaterialTheme.spacing.extraLarge),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)) {
                    Text(
                        text = stringResource(Res.string.import_dialog_header_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(Res.string.import_dialog_header_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Title + Artist
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large)) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    Text(
                        stringResource(Res.string.import_dialog_song_title_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = viewModel::updateTitle,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(Res.string.import_dialog_song_title_placeholder)) },
                        colors = textFieldColors,
                        shape = MaterialTheme.shapes.medium,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    Text(
                        stringResource(Res.string.import_dialog_artist_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = state.artist,
                        onValueChange = viewModel::updateArtist,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(Res.string.import_dialog_artist_placeholder)) },
                        colors = textFieldColors,
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            }

            // Lyrics
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Title,
                            contentDescription = null,
                            modifier = Modifier.size(MaterialTheme.spacing.large),
                        )
                        Text(
                            stringResource(Res.string.import_dialog_lyrics_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        ),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = null,
                                modifier = Modifier.size(MaterialTheme.spacing.large),
                            )
                            Text(stringResource(Res.string.import_dialog_ocr_button))
                        }
                    }
                }
                OutlinedTextField(
                    value = state.lyrics,
                    onValueChange = viewModel::updateLyrics,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    minLines = 10,
                    placeholder = { Text(stringResource(Res.string.import_dialog_lyrics_placeholder)) },
                    colors = textFieldColors,
                    shape = MaterialTheme.shapes.medium,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    Icon(
                        imageVector = Icons.Default.Title,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(MaterialTheme.spacing.large),
                    )
                    Text(
                        text = stringResource(Res.string.import_dialog_lyrics_tip),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    MaterialTheme.spacing.medium,
                    Alignment.End,
                ),
            ) {
                TextButton(onClick = { viewModel.onBack() }) {
                    Text(stringResource(Res.string.import_dialog_cancel))
                }
                Button(
                    onClick = { viewModel.onManualInputConfirmed() },
                    enabled = state.title.isNotBlank() && !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    ),
                ) {
                    Text(stringResource(Res.string.import_dialog_confirm))
                }
            }
        }
    }
}

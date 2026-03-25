package com.pointlessapps.songbook.library.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.library.ImportSongEvent
import com.pointlessapps.songbook.library.ImportSongViewModel
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.import_dialog_artist_label
import com.pointlessapps.songbook.shared.import_dialog_artist_placeholder
import com.pointlessapps.songbook.shared.import_dialog_camera_button
import com.pointlessapps.songbook.shared.import_dialog_cancel
import com.pointlessapps.songbook.shared.import_dialog_confirm
import com.pointlessapps.songbook.shared.import_dialog_gallery_button
import com.pointlessapps.songbook.shared.import_dialog_header_title
import com.pointlessapps.songbook.shared.import_dialog_lyrics_editor
import com.pointlessapps.songbook.shared.import_dialog_lyrics_placeholder
import com.pointlessapps.songbook.shared.import_dialog_lyrics_tip
import com.pointlessapps.songbook.shared.import_dialog_ocr_button
import com.pointlessapps.songbook.shared.import_dialog_ocr_subtitle
import com.pointlessapps.songbook.shared.import_dialog_song_identity
import com.pointlessapps.songbook.shared.import_dialog_song_title_label
import com.pointlessapps.songbook.shared.import_dialog_song_title_placeholder
import com.pointlessapps.songbook.ui.theme.spacing
import com.preat.peekaboo.image.picker.FilterOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.ui.camera.CameraMode
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .imePadding()
                .padding(MaterialTheme.spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            // OCR Import Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.extraLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                shape = CircleShape,
                            )
                            .padding(MaterialTheme.spacing.medium),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CropFree,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(MaterialTheme.spacing.extraLarge),
                        )
                    }
                    Text(
                        text = stringResource(Res.string.import_dialog_ocr_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = stringResource(Res.string.import_dialog_ocr_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                    if (state.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                        OcrActionButton(
                            label = stringResource(Res.string.import_dialog_camera_button),
                            icon = Icons.Default.CameraAlt,
                            onClick = { viewModel.onCameraRequested() },
                            style = OcrButtonStyle.Outlined,
                        )
                        OcrActionButton(
                            label = stringResource(Res.string.import_dialog_gallery_button),
                            icon = Icons.Default.Photo,
                            onClick = { imagePickerLauncher.launch() },
                            style = OcrButtonStyle.Filled,
                        )
                    }
                }
            }

            // Song Identity
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                SectionHeader(stringResource(Res.string.import_dialog_song_identity))
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large)) {
                    LabeledTextField(
                        label = stringResource(Res.string.import_dialog_song_title_label),
                        value = state.title,
                        onValueChange = viewModel::updateTitle,
                        placeholder = stringResource(Res.string.import_dialog_song_title_placeholder),
                        modifier = Modifier.weight(1f),
                    )
                    LabeledTextField(
                        label = stringResource(Res.string.import_dialog_artist_label),
                        value = state.artist,
                        onValueChange = viewModel::updateArtist,
                        placeholder = stringResource(Res.string.import_dialog_artist_placeholder),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Lyrics & Chords Editor
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                SectionHeader(stringResource(Res.string.import_dialog_lyrics_editor))
                OutlinedTextField(
                    value = state.lyrics,
                    onValueChange = viewModel::updateLyrics,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    minLines = 10,
                    placeholder = { Text(stringResource(Res.string.import_dialog_lyrics_placeholder)) },
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

    if (state.showCamera) {
        PeekabooCamera(
            state = rememberPeekabooCameraState(
                initialCameraMode = CameraMode.Back,
                onCapture = { viewModel.onCameraCaptureDone(it) },
            ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private enum class OcrButtonStyle { Outlined, Filled }

@Composable
private fun OcrActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    style: OcrButtonStyle,
    modifier: Modifier = Modifier,
) {
    val contentColor = when (style) {
        OcrButtonStyle.Outlined -> MaterialTheme.colorScheme.onPrimaryContainer
        OcrButtonStyle.Filled -> MaterialTheme.colorScheme.primaryContainer
    }
    val containerColor = when (style) {
        OcrButtonStyle.Outlined -> MaterialTheme.colorScheme.primaryContainer
        OcrButtonStyle.Filled -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val content: @Composable () -> Unit = {
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(MaterialTheme.spacing.large),
            )
            Text(label)
        }
    }
    when (style) {
        OcrButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = contentColor,
                containerColor = containerColor,
            ),
            border = BorderStroke(width = Dp.Hairline, color = contentColor),
            content = { content() },
        )

        OcrButtonStyle.Filled -> Button(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            content = { content() },
        )
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder) },
            shape = MaterialTheme.shapes.medium,
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.extraSmall,
                ),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

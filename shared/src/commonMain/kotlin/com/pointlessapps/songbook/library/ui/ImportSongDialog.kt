package com.pointlessapps.songbook.library.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pointlessapps.songbook.core.utils.EdgeToEdgeDialogProperties
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_artist_label
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_artist_placeholder
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_cancel
import com.pointlessapps.songbook.shared.generated.resources.import_dialog_close
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
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ImportSongDialog(
    initialOcrText: String?,
    onDismiss: () -> Unit,
    onOcrRequested: () -> Unit,
    onManualConfirmed: (title: String, artist: String, lyricsText: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var lyrics by remember(initialOcrText) { mutableStateOf(initialOcrText ?: "") }
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        cursorColor = MaterialTheme.colorScheme.primary,
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = EdgeToEdgeDialogProperties,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .systemBarsPadding()
                    .imePadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
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
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.import_dialog_close),
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
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text(stringResource(Res.string.import_dialog_song_title_placeholder)) },
                            colors = textFieldColors,
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
                            value = artist,
                            onValueChange = { artist = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text(stringResource(Res.string.import_dialog_artist_placeholder)) },
                            colors = textFieldColors,
                        )
                    }
                }

                // Lyrics
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Title,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            stringResource(Res.string.import_dialog_lyrics_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = onOcrRequested,
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
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(stringResource(Res.string.import_dialog_ocr_button))
                            }
                        }
                    }
                    OutlinedTextField(
                        value = lyrics,
                        onValueChange = { lyrics = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                        minLines = 10,
                        placeholder = { Text(stringResource(Res.string.import_dialog_lyrics_placeholder)) },
                        colors = textFieldColors,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Title,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp),
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
                    TextButton(onClick = onDismiss) { Text(stringResource(Res.string.import_dialog_cancel)) }
                    Button(
                        onClick = { onManualConfirmed(title, artist, lyrics) },
                        enabled = title.isNotBlank(),
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
}

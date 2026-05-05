package com.pointlessapps.songbook.lyrics.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_confirm
import com.pointlessapps.songbook.shared.ui.common_decrement
import com.pointlessapps.songbook.shared.ui.common_increment
import com.pointlessapps.songbook.shared.ui.common_select_text_scale
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconMinus
import com.pointlessapps.songbook.ui.theme.IconPlus
import com.pointlessapps.songbook.ui.theme.IconTextSize
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TextScaleDialog(
    textScale: Int,
    minTextScale: Int,
    maxTextScale: Int,
    onTextScaleSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var currentTextScale by rememberSaveable { mutableStateOf(textScale) }

    SongbookDialog(
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = stringResource(Res.string.common_select_text_scale),
            icon = IconTextSize,
            dismissible = SongbookDialogDismissible.OnBackPress,
        ),
    ) {
        Counter(
            value = currentTextScale,
            onDecrementClicked = {
                currentTextScale = ((currentTextScale - 1) / 10 * 10)
                    .coerceIn(minTextScale, maxTextScale)
            },
            onIncrementClicked = {
                currentTextScale = (currentTextScale / 10 * 10 + 10)
                    .coerceIn(minTextScale, maxTextScale)
            },
        )

        SongbookButton(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.common_confirm),
            onClick = { onTextScaleSelected(currentTextScale) },
            buttonStyle = defaultSongbookButtonStyle().copy(
                containerColor = MaterialTheme.colorScheme.primary,
                textStyle = defaultSongbookTextStyle().copy(
                    textAlign = TextAlign.Center,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ),
        )
    }
}

@Composable
private fun Counter(
    value: Int,
    onDecrementClicked: () -> Unit,
    onIncrementClicked: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
    ) {
        SongbookIconButton(
            modifier = Modifier.padding(MaterialTheme.spacing.small),
            icon = IconMinus,
            tooltipLabel = Res.string.common_decrement,
            onClick = onDecrementClicked,
            iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                outlineColor = MaterialTheme.colorScheme.outline,
            ),
        )

        SongbookText(
            text = "$value%",
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.titleLarge,
            ),
        )

        SongbookIconButton(
            modifier = Modifier.padding(MaterialTheme.spacing.small),
            icon = IconPlus,
            tooltipLabel = Res.string.common_increment,
            onClick = onIncrementClicked,
            iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                outlineColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

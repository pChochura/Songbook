package com.pointlessapps.songbook.lyrics.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_confirm
import com.pointlessapps.songbook.shared.common_decrement
import com.pointlessapps.songbook.shared.common_increment
import com.pointlessapps.songbook.shared.common_select_key_offset
import com.pointlessapps.songbook.shared.common_select_key_offset_description
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconKey
import com.pointlessapps.songbook.ui.theme.IconMinus
import com.pointlessapps.songbook.ui.theme.IconPlus
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun KeyOffsetDialog(
    keyOffset: Int,
    onKeyOffsetSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var currentKeyOffset by rememberSaveable { mutableStateOf(keyOffset) }

    SongbookDialog(
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = stringResource(Res.string.common_select_key_offset),
            icon = IconKey,
            dismissible = SongbookDialogDismissible.OnBackPress,
        ),
    ) {
        SongbookText(
            text = stringResource(Res.string.common_select_key_offset_description),
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.bodyMedium,
                textColor = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            ),
        )

        Counter(
            value = currentKeyOffset,
            onDecrementClicked = { currentKeyOffset-- },
            onIncrementClicked = { currentKeyOffset++ },
        )

        SongbookButton(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.common_confirm),
            onClick = { onKeyOffsetSelected(currentKeyOffset) },
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
            icon = IconMinus,
            tooltipLabel = Res.string.common_decrement,
            onClick = onDecrementClicked,
        )

        SongbookText(
            text = "${if (value > 0) "+" else ""}$value",
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.titleLarge,
            ),
        )

        SongbookIconButton(
            icon = IconPlus,
            tooltipLabel = Res.string.common_increment,
            onClick = onIncrementClicked,
        )
    }
}

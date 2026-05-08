package com.pointlessapps.songbook.lyrics.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.BottomBarPadding.Companion.bottomBarHeight
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_decrement
import com.pointlessapps.songbook.shared.ui.common_increment
import com.pointlessapps.songbook.ui.BottomBarBackground
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconMinus
import com.pointlessapps.songbook.ui.theme.IconPlus
import com.pointlessapps.songbook.ui.theme.spacing

@Composable
internal fun KeyOffsetFab(
    keyOffset: Int,
    onKeyOffsetChanged: (Int) -> Unit,
    onClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.End,
    ) {
        BottomBarBackground {
            Row(
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = onClicked,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                SongbookIconButton(
                    modifier = Modifier.padding(MaterialTheme.spacing.small),
                    icon = IconMinus,
                    tooltipLabel = Res.string.common_decrement,
                    onClick = { onKeyOffsetChanged(keyOffset - 1) },
                    iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = Color.Transparent,
                        outlineColor = Color.Transparent,
                    ),
                )

                SongbookText(
                    text = "${if (keyOffset > 0) "+" else ""}$keyOffset",
                    textStyle = defaultSongbookTextStyle().copy(
                        textAlign = TextAlign.Center,
                        typography = MaterialTheme.typography.titleLarge,
                        textColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )

                SongbookIconButton(
                    modifier = Modifier.padding(MaterialTheme.spacing.small),
                    icon = IconPlus,
                    tooltipLabel = Res.string.common_increment,
                    onClick = { onKeyOffsetChanged(keyOffset + 1) },
                    iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = Color.Transparent,
                        outlineColor = Color.Transparent,
                    ),
                )
            }
        }

        Spacer(Modifier.bottomBarHeight())
    }
}

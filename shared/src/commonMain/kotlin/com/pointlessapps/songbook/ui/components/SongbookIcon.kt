package com.pointlessapps.songbook.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SongbookIcon(
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    iconStyle: SongbookIconStyle = defaultSongbookIconStyle(),
) = Icon(
    painter = painterResource(icon),
    modifier = modifier,
    tint = iconStyle.tint,
    contentDescription = contentDescription,
)

@Composable
fun defaultSongbookIconStyle() = SongbookIconStyle(
    tint = MaterialTheme.colorScheme.onSurface,
)

data class SongbookIconStyle(
    val tint: Color,
)

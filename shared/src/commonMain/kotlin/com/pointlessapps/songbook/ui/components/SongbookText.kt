package com.pointlessapps.songbook.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SongbookText(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: SongbookTextStyle = defaultSongbookTextStyle(),
) = SongbookText(
    text = AnnotatedString(text),
    modifier = modifier,
    textStyle = textStyle,
)

@Composable
fun SongbookText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    textStyle: SongbookTextStyle = defaultSongbookTextStyle(),
) = Text(
    modifier = modifier,
    text = text,
    style = textStyle.typography.copy(
        color = textStyle.textColor,
        textAlign = textStyle.textAlign,
    ),
    overflow = textStyle.textOverflow,
    maxLines = textStyle.maxLines,
)

@Composable
fun defaultSongbookTextStyle() = SongbookTextStyle(
    textColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    typography = MaterialTheme.typography.bodyMedium,
    textAlign = TextAlign.Start,
    textOverflow = TextOverflow.Visible,
    maxLines = Int.MAX_VALUE,
)

data class SongbookTextStyle(
    val textColor: Color,
    val disabledTextColor: Color,
    val textAlign: TextAlign,
    val typography: TextStyle,
    val textOverflow: TextOverflow,
    val maxLines: Int,
)

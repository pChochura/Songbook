package com.pointlessapps.songbook.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SongbookText(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: SongbookTextStyle = defaultSongbookTextStyle(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
) = SongbookText(
    text = AnnotatedString(text),
    modifier = modifier,
    textStyle = textStyle,
    onTextLayout = onTextLayout,
)

@Composable
fun SongbookText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    textStyle: SongbookTextStyle = defaultSongbookTextStyle(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
) = Text(
    modifier = modifier,
    text = text,
    style = textStyle.typography.copy(
        color = textStyle.textColor,
        textAlign = textStyle.textAlign,
    ),
    overflow = textStyle.textOverflow,
    maxLines = textStyle.maxLines,
    softWrap = textStyle.softWrap,
    onTextLayout = onTextLayout,
)

@Composable
fun defaultSongbookTextStyle() = SongbookTextStyle(
    textColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    typography = MaterialTheme.typography.bodyMedium,
    textAlign = TextAlign.Start,
    textOverflow = TextOverflow.Visible,
    maxLines = Int.MAX_VALUE,
    softWrap = true,
)

data class SongbookTextStyle(
    val textColor: Color,
    val disabledTextColor: Color,
    val textAlign: TextAlign,
    val typography: TextStyle,
    val textOverflow: TextOverflow,
    val maxLines: Int,
    val softWrap: Boolean,
)

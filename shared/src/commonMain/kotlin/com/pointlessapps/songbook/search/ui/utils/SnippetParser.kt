package com.pointlessapps.songbook.search.ui.utils

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

internal fun parseSnippet(snippet: String, highlightStyle: SpanStyle) = buildAnnotatedString {
    val parts = snippet.split("<b>", "</b>")
    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) {
            pushStyle(highlightStyle)
            append(part)
            pop()
        } else {
            append(part)
        }
    }
}

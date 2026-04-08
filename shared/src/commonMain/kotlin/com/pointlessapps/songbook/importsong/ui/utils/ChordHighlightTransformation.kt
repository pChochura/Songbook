package com.pointlessapps.songbook.importsong.ui.utils

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

private val bracketRegex = "\\[(.*?)]".toRegex()
internal val ChordHighlightTransformation: OutputTransformation
    @Composable
    get() {
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary

        return OutputTransformation {
            bracketRegex.findAll(originalText).forEach { match ->
                val content = match.groupValues[1]
                val range = match.range

                val isHeader = content.startsWith("verse", ignoreCase = true) ||
                        content.startsWith("chorus", ignoreCase = true) ||
                        content.startsWith("bridge", ignoreCase = true) ||
                        content.startsWith("intro", ignoreCase = true) ||
                        content.startsWith("outro", ignoreCase = true)

                val style = if (isHeader) {
                    SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    SpanStyle(
                        color = secondaryColor,
                        fontWeight = FontWeight.Bold,
                    )
                }

                addStyle(style, range.first, range.last + 1)
            }
        }
    }

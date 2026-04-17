package com.pointlessapps.songbook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun platformColorPalette(
    lightColorPalette: ColorScheme,
    darkColorPalette: ColorScheme,
): ColorScheme = when {
    isSystemInDarkTheme() -> darkColorPalette
    else -> lightColorPalette
}

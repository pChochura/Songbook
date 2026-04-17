package com.pointlessapps.songbook.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun platformColorPalette(
    lightColorPalette: ColorScheme,
    darkColorPalette: ColorScheme,
): ColorScheme {
    val isDarkTheme = isSystemInDarkTheme()
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    return when {
        dynamicColor && isDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
        dynamicColor && !isDarkTheme -> dynamicLightColorScheme(LocalContext.current)
        isDarkTheme -> darkColorPalette
        else -> lightColorPalette
    }
}

package com.pointlessapps.songbook.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.montserrat_bold
import com.pointlessapps.songbook.shared.montserrat_light
import com.pointlessapps.songbook.shared.montserrat_medium
import com.pointlessapps.songbook.shared.montserrat_normal
import com.pointlessapps.songbook.shared.montserrat_semi_bold
import org.jetbrains.compose.resources.Font

@Composable
private fun typography(): Typography {
    val fontFamily = FontFamily(
        Font(
            resource = Res.font.montserrat_light,
            weight = FontWeight.Light,
        ),
        Font(
            resource = Res.font.montserrat_normal,
            weight = FontWeight.Normal,
        ),
        Font(
            resource = Res.font.montserrat_medium,
            weight = FontWeight.Medium,
        ),
        Font(
            resource = Res.font.montserrat_semi_bold,
            weight = FontWeight.SemiBold,
        ),
        Font(
            resource = Res.font.montserrat_bold,
            weight = FontWeight.Bold,
        ),
    )

    return Typography().run {
        copy(
            displayLarge = displayLarge.copy(fontFamily = fontFamily),
            displayMedium = displayMedium.copy(fontFamily = fontFamily),
            displaySmall = displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = titleLarge.copy(fontFamily = fontFamily),
            titleMedium = titleMedium.copy(fontFamily = fontFamily),
            titleSmall = titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = bodySmall.copy(fontFamily = fontFamily),
            labelLarge = labelLarge.copy(fontFamily = fontFamily),
            labelMedium = labelMedium.copy(fontFamily = fontFamily),
            labelSmall = labelSmall.copy(fontFamily = fontFamily),
        )
    }
}

@Composable
private fun shapes() = Shapes(
    small = RoundedCornerShape(SMALL_CORNER_RADIUS),
    medium = RoundedCornerShape(MEDIUM_CORNER_RADIUS),
    large = RoundedCornerShape(LARGE_CORNER_RADIUS),
)

@Composable
private fun lightColorPalette() = lightColorScheme(
    primary = lightPrimary,
    onPrimary = lightOnPrimary,
    secondary = lightSecondary,
    onSecondary = lightOnSecondary,
    background = lightBackground,
    onBackground = lightOnBackground,
    surface = lightSurface,
    onSurface = lightOnSurface,
    onSurfaceVariant = lightOnSurfaceVariant,
    error = lightError,
    onError = lightOnError,
    outline = lightOutline,
    outlineVariant = lightOutlineVariant,
    surfaceContainer = lightSurfaceContainer,
    surfaceContainerHigh = lightSurfaceContainerHigh,
    surfaceContainerLow = lightSurfaceContainerLow,
    primaryContainer = lightPrimaryContainer,
    onPrimaryContainer = lightOnPrimaryContainer,
    inverseSurface = lightInverseSurface,
    inverseOnSurface = lightInverseOnSurface,
)

@Composable
private fun darkColorPalette() = darkColorScheme(
    primary = darkPrimary,
    onPrimary = darkOnPrimary,
    secondary = darkSecondary,
    onSecondary = darkOnSecondary,
    background = darkBackground,
    onBackground = darkOnBackground,
    surface = darkSurface,
    onSurface = darkOnSurface,
    onSurfaceVariant = darkOnSurfaceVariant,
    error = darkError,
    onError = darkOnError,
    outline = darkOutline,
    outlineVariant = darkOutlineVariant,
    surfaceContainer = darkSurfaceContainer,
    surfaceContainerHigh = darkSurfaceContainerHigh,
    surfaceContainerLow = darkSurfaceContainerLow,
    primaryContainer = darkPrimaryContainer,
    onPrimaryContainer = darkOnPrimaryContainer,
    inverseSurface = darkInverseSurface,
    inverseOnSurface = darkInverseOnSurface,
)

@Composable
internal fun SongbookTheme(
    lightColorPalette: ColorScheme = lightColorPalette(),
    darkColorPalette: ColorScheme = darkColorPalette(),
    typography: Typography = typography(),
    shapes: Shapes = shapes(),
    content: @Composable () -> Unit,
) = MaterialTheme(
    colorScheme = platformColorPalette(
        lightColorPalette = lightColorPalette,
        darkColorPalette = darkColorPalette,
    ),
    typography = typography,
    shapes = shapes,
    content = content,
)

@Composable
internal expect fun platformColorPalette(
    lightColorPalette: ColorScheme,
    darkColorPalette: ColorScheme,
): ColorScheme

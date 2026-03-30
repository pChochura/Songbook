package com.pointlessapps.songbook.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun PaddingValues.add(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + other.calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + other.calculateTopPadding(),
        end = calculateEndPadding(layoutDirection) + other.calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + other.calculateBottomPadding(),
    )
}

@Composable
internal fun PaddingValues.add(all: Dp = 0.dp): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + all,
        top = calculateTopPadding() + all,
        end = calculateEndPadding(layoutDirection) + all,
        bottom = calculateBottomPadding() + all,
    )
}

@Composable
internal fun PaddingValues.add(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + start,
        top = calculateTopPadding() + top,
        end = calculateEndPadding(layoutDirection) + end,
        bottom = calculateBottomPadding() + bottom,
    )
}

@Composable
internal fun PaddingValues.add(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + horizontal,
        top = calculateTopPadding() + vertical,
        end = calculateEndPadding(layoutDirection) + horizontal,
        bottom = calculateBottomPadding() + vertical,
    )
}

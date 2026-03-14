package com.pointlessapps.songbook.core.utils

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
actual fun PlatformDialogProperties(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    securePolicy: SecureFlagPolicy,
    usePlatformDefaultWidth: Boolean,
    decorFitsSystemWindows: Boolean,
    windowTitle: String,
    usePlatformInsets: Boolean,
    useSoftwareKeyboardInset: Boolean,
    scrimColor: Color,
) = DialogProperties(
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    usePlatformInsets = usePlatformInsets,
    useSoftwareKeyboardInset = useSoftwareKeyboardInset,
    scrimColor = scrimColor,
)

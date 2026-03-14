package com.pointlessapps.songbook.core.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

val EdgeToEdgeDialogProperties: DialogProperties = PlatformDialogProperties(
    dismissOnBackPress = true,
    dismissOnClickOutside = true,
    usePlatformDefaultWidth = true,
    usePlatformInsets = false,
    decorFitsSystemWindows = false,
)

@Suppress("FunctionName")
expect fun PlatformDialogProperties(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    usePlatformDefaultWidth: Boolean = true,
    decorFitsSystemWindows: Boolean = true,
    windowTitle: String = "",
    usePlatformInsets: Boolean = false,
    useSoftwareKeyboardInset: Boolean = false,
    scrimColor: Color = Color.Transparent,
): DialogProperties

enum class SecureFlagPolicy { Inherit, SecureOn, SecureOff }

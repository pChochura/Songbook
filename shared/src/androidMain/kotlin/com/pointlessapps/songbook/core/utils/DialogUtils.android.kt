package com.pointlessapps.songbook.core.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy as AndroidSecureFlagPolicy

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
    securePolicy = when (securePolicy) {
        SecureFlagPolicy.Inherit -> AndroidSecureFlagPolicy.Inherit
        SecureFlagPolicy.SecureOn -> AndroidSecureFlagPolicy.SecureOn
        SecureFlagPolicy.SecureOff -> AndroidSecureFlagPolicy.SecureOff
    },
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    decorFitsSystemWindows = decorFitsSystemWindows,
    windowTitle = windowTitle,
)

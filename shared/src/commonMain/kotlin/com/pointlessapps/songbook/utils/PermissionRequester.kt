package com.pointlessapps.songbook.utils

import androidx.compose.runtime.Composable

internal interface PermissionRequester {
    suspend fun requestCameraPermission(): Boolean
}

@Composable
internal expect fun rememberPermissionRequester(): PermissionRequester

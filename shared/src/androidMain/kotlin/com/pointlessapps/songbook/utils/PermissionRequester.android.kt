package com.pointlessapps.songbook.utils

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
internal actual fun rememberPermissionRequester(): PermissionRequester {
    var permissionContinuation by remember { mutableStateOf<CancellableContinuation<Boolean>?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        permissionContinuation?.resume(isGranted)
        permissionContinuation = null
    }

    return remember(launcher) {
        object : PermissionRequester {
            override suspend fun requestCameraPermission(): Boolean =
                suspendCancellableCoroutine { continuation ->
                    permissionContinuation = continuation

                    launcher.launch(Manifest.permission.CAMERA)

                    continuation.invokeOnCancellation {
                        permissionContinuation = null
                    }
                }
        }
    }
}

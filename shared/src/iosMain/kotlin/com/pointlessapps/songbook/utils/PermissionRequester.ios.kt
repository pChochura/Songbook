package com.pointlessapps.songbook.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

private class IosPermissionRequester : PermissionRequester {
    override suspend fun requestCameraPermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)

            when (status) {
                AVAuthorizationStatusNotDetermined -> {
                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                        dispatch_async(dispatch_get_main_queue()) {
                            continuation.resume(granted)
                        }
                    }
                }

                AVAuthorizationStatusAuthorized -> continuation.resume(true)
                else -> continuation.resume(false)
            }
        }
}

@Composable
internal actual fun rememberPermissionRequester(): PermissionRequester =
    remember { IosPermissionRequester() }

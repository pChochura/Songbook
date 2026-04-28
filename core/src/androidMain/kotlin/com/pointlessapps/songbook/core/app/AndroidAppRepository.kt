package com.pointlessapps.songbook.core.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

internal class AndroidAppRepository(
    private val context: Context,
) : AppRepository {
    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}

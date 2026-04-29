package com.pointlessapps.songbook.core.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri

internal class AndroidAppRepository(
    private val context: Context,
    private val removeAccountUrl: String,
) : AppRepository {
    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    override fun openRemoveAccountWebsite(accessToken: String, refreshToken: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            removeAccountUrl.toUri().buildUpon()
                .appendQueryParameter(ACCESS_TOKEN, accessToken)
                .appendQueryParameter(REFRESH_TOKEN, refreshToken)
                .build(),
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
    }
}

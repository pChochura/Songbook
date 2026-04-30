package com.pointlessapps.songbook.core.app

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

internal class IosAppRepository(
    private val removeAccountUrl: String,
) : AppRepository {
    override fun openAppSettings() {
        val settingsUrl = NSURL(string = UIApplicationOpenSettingsURLString)
        if (UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
            UIApplication.sharedApplication.openURL(settingsUrl)
        }
    }

    override fun openRemoveAccountWebsite(accessToken: String, refreshToken: String) {
        val websiteUrl = NSURL(
            string = "$removeAccountUrl?$ACCESS_TOKEN=$accessToken&$REFRESH_TOKEN=$refreshToken",
        )

        if (UIApplication.sharedApplication.canOpenURL(websiteUrl)) {
            UIApplication.sharedApplication.openURL(websiteUrl)
        }
    }

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
    }
}

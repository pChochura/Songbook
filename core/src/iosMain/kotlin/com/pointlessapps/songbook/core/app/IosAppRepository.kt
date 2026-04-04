package com.pointlessapps.songbook.core.app

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

internal class IosAppRepository : AppRepository {
    override fun openAppSettings() {
        val settingsUrl = NSURL(string = UIApplicationOpenSettingsURLString)
        if (UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
            UIApplication.sharedApplication.openURL(settingsUrl)
        }
    }
}

package com.pointlessapps.songbook

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.pointlessapps.songbook.di.initKoin
import org.koin.android.ext.koin.androidContext

class SongbookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@SongbookApp)
        }
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}

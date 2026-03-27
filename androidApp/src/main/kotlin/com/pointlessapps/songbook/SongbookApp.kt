package com.pointlessapps.songbook

import android.app.Application
import com.pointlessapps.songbook.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.fileProperties

class SongbookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            fileProperties()
            androidContext(this@SongbookApp)
        }
    }
}

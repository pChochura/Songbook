package com.pointlessapps.songbook

import android.app.Application
import com.pointlessapps.songbook.di.androidDatabaseModule
import com.pointlessapps.songbook.di.initKoin
import org.koin.android.ext.koin.androidContext

class SongbookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            appDeclaration = {
                androidContext(this@SongbookApp)
            },
            platformModules = listOf(androidDatabaseModule)
        )
    }
}

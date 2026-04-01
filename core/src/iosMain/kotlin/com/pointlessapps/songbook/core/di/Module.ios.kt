package com.pointlessapps.songbook.core.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pointlessapps.songbook.core.database.AppDatabase
import com.pointlessapps.songbook.core.database.AppDatabaseConstructor
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

internal actual val platformModule = module {
    single<AppDatabase> {
        val dbFilePath = NSHomeDirectory() + "/songbook.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
            factory = { AppDatabaseConstructor.initialize() }
        ).setDriver(BundledSQLiteDriver())
            .build()
    }
}

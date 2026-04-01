package com.pointlessapps.songbook.core.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pointlessapps.songbook.core.database.AppDatabase
import com.pointlessapps.songbook.core.database.AppDatabaseConstructor
import org.koin.dsl.module

internal actual val platformModule = module {
    single<AppDatabase> {
        val dbFile = get<android.content.Context>().getDatabasePath("songbook.db")
        Room.databaseBuilder<AppDatabase>(
            context = get(),
            name = dbFile.absolutePath,
            factory = { AppDatabaseConstructor.initialize() }
        ).setDriver(BundledSQLiteDriver())
            .build()
    }
}

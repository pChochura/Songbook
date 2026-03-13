package com.pointlessapps.songbook.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.pointlessapps.songbook.data.AppDatabase
import org.koin.dsl.module

val androidDatabaseModule = module {
    single {
        val context = get<Context>()
        val dbFile = context.getDatabasePath("app_database.db")
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath,
        ).setDriver(AndroidSQLiteDriver())
    }
}

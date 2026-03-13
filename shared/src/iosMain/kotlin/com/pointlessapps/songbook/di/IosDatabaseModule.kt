package com.pointlessapps.songbook.di

import androidx.room.Room
import androidx.sqlite.driver.NativeSQLiteDriver
import com.pointlessapps.songbook.data.AppDatabase
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

val iosDatabaseModule = module {
    single {
        val dbFilePath = NSHomeDirectory() + "/Documents/app_database.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
        ).setDriver(NativeSQLiteDriver())
    }
}

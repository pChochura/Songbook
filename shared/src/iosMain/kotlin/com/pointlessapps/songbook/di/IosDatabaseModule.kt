package com.pointlessapps.songbook.di

import androidx.room.Room
import com.pointlessapps.songbook.data.AppDatabase
import com.pointlessapps.songbook.data.AppDatabaseConstructor
import platform.Foundation.NSHomeDirectory
import org.koin.dsl.module

val iosDatabaseModule = module {
    single {
        val dbFilePath = NSHomeDirectory() + "/Documents/app_database.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
            factory = { AppDatabaseConstructor.initialize() }
        )
    }
}

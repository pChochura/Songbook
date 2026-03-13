package com.pointlessapps.songbook.di

import android.content.Context
import androidx.room.Room
import com.pointlessapps.songbook.data.AppDatabase
import com.pointlessapps.songbook.data.AppDatabaseConstructor
import org.koin.dsl.module

val androidDatabaseModule = module {
    single {
        val context = get<Context>()
        val dbFile = context.getDatabasePath("app_database.db")
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath,
            factory = { AppDatabaseConstructor.initialize() }
        )
    }
}

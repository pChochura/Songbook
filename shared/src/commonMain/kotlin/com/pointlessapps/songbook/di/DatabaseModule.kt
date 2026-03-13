package com.pointlessapps.songbook.di

import androidx.room.RoomDatabase
import com.pointlessapps.songbook.data.AppDatabase
import com.pointlessapps.songbook.data.getDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        getDatabase(get<RoomDatabase.Builder<AppDatabase>>())
    }
    single { get<AppDatabase>().songDao() }
}

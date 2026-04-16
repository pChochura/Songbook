package com.pointlessapps.songbook.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pointlessapps.songbook.core.app.di.appModule
import com.pointlessapps.songbook.core.auth.AndroidGoogleAuthManager
import com.pointlessapps.songbook.core.auth.GoogleAuthManager
import com.pointlessapps.songbook.core.database.AppDatabase
import com.pointlessapps.songbook.core.database.AppDatabaseConstructor
import com.pointlessapps.songbook.core.network.NetworkRepository
import com.pointlessapps.songbook.core.network.NetworkRepositoryImpl
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule = module {
    single<AppDatabase> {
        val dbFile = get<android.content.Context>().getDatabasePath("songbook.db")
        Room.databaseBuilder<AppDatabase>(
            context = get(),
            name = dbFile.absolutePath,
            factory = { AppDatabaseConstructor.initialize() },
        ).setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                androidContext()
                    .filesDir
                    .resolve("songbook.preferences_pb")
                    .absolutePath
                    .toPath()
            },
        )
    }

    singleOf(::NetworkRepositoryImpl).bind<NetworkRepository>()
    single<GoogleAuthManager> {
        AndroidGoogleAuthManager(
            context = androidContext(),
            webClientId = getProperty("GOOGLE_WEB_CLIENT_ID"),
        )
    }

    includes(appModule)
}

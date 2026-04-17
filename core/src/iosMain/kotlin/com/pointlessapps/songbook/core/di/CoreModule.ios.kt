package com.pointlessapps.songbook.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pointlessapps.songbook.core.app.di.appModule
import com.pointlessapps.songbook.core.database.AppDatabase
import com.pointlessapps.songbook.core.database.AppDatabaseConstructor
import com.pointlessapps.songbook.core.network.IosNetworkRepository
import com.pointlessapps.songbook.core.network.NetworkRepository
import com.pointlessapps.songbook.core.utils.documentDirectory
import okio.Path.Companion.toPath
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

internal actual val platformModule = module {
    single<AppDatabase> {
        val dbFilePath = NSHomeDirectory() + "/songbook.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
            factory = { AppDatabaseConstructor.initialize() },
        ).setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { (documentDirectory() + "/songbook.preferences_pb").toPath() },
        )
    }

    singleOf(::IosNetworkRepository).bind<NetworkRepository>()

    includes(appModule)
}

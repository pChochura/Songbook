package com.pointlessapps.songbook.core.di

import com.pointlessapps.songbook.core.auth.di.authModule
import com.pointlessapps.songbook.core.database.AppDatabase
import com.pointlessapps.songbook.core.prefs.di.prefsModule
import com.pointlessapps.songbook.core.setlist.di.setlistModule
import com.pointlessapps.songbook.core.song.di.songModule
import com.pointlessapps.songbook.core.sync.di.syncModule
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformModule: Module

val coreModule = module {
    includes(platformModule)

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
        }
    }

    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = getProperty("SUPABASE_URL"),
            supabaseKey = getProperty("SUPABASE_KEY"),
        ) {
            install(Realtime)
            install(Postgrest)
            install(Auth)
        }
    }

    single { get<AppDatabase>().songDao() }
    single { get<AppDatabase>().setlistDao() }
    single { get<AppDatabase>().syncActionDao() }
    single { get<AppDatabase>().syncDao() }

    includes(syncModule)
    includes(authModule)
    includes(songModule)
    includes(setlistModule)
    includes(prefsModule)
}

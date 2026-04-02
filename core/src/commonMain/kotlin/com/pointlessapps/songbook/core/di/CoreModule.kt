package com.pointlessapps.songbook.core.di

import com.pointlessapps.songbook.core.auth.di.authModule
import com.pointlessapps.songbook.core.database.AppDatabase
import com.pointlessapps.songbook.core.prefs.di.prefsModule
import com.pointlessapps.songbook.core.setlist.di.setlistModule
import com.pointlessapps.songbook.core.song.di.songModule
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformModule: Module

val coreModule = module {
    includes(platformModule)

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

    includes(authModule)
    includes(songModule)
    includes(setlistModule)
    includes(prefsModule)
}

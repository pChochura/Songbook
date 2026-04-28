package com.pointlessapps.songbook.core.di

import com.pointlessapps.songbook.core.BuildKonfig
import com.pointlessapps.songbook.core.auth.GoogleAuthManager
import com.pointlessapps.songbook.core.auth.WasmGoogleAuthManager
import com.pointlessapps.songbook.core.auth.di.authModule
import com.pointlessapps.songbook.core.prefs.di.prefsModule
import com.pointlessapps.songbook.core.queue.di.queueModule
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
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule = module {
}

actual val coreModule = module {
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
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_KEY,
        ) {
            install(Realtime)
            install(Postgrest)
            install(Auth)
        }
    }

    includes(syncModule)
    includes(authModule)
    includes(songModule)
    includes(setlistModule)
    includes(prefsModule)
    includes(queueModule)
    singleOf(::WasmGoogleAuthManager).bind<GoogleAuthManager>()
}

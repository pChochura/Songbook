package com.pointlessapps.songbook.core.di

import com.pointlessapps.songbook.core.auth.GoogleAuthManager
import com.pointlessapps.songbook.core.auth.WasmGoogleAuthManager
import com.pointlessapps.songbook.core.auth.di.authModule
import com.pointlessapps.songbook.core.prefs.di.prefsModule
import com.pointlessapps.songbook.core.queue.di.queueModule
import com.pointlessapps.songbook.core.setlist.di.setlistModule
import com.pointlessapps.songbook.core.song.di.songModule
import com.pointlessapps.songbook.core.supabase.di.supabaseModule
import com.pointlessapps.songbook.core.sync.di.syncModule
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule = module {
    singleOf(::WasmGoogleAuthManager).bind<GoogleAuthManager>()
}

actual val coreModule = module {
    includes(platformModule)

    includes(supabaseModule)
    includes(syncModule)
    includes(authModule)
    includes(songModule)
    includes(setlistModule)
    includes(prefsModule)
    includes(queueModule)
}

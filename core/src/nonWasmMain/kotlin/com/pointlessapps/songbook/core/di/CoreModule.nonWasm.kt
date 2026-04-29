package com.pointlessapps.songbook.core.di

import com.pointlessapps.songbook.core.auth.di.authModule
import com.pointlessapps.songbook.core.database.AppDatabase
import com.pointlessapps.songbook.core.prefs.di.prefsModule
import com.pointlessapps.songbook.core.queue.di.queueModule
import com.pointlessapps.songbook.core.setlist.di.setlistModule
import com.pointlessapps.songbook.core.song.di.songModule
import com.pointlessapps.songbook.core.supabase.di.supabaseModule
import com.pointlessapps.songbook.core.sync.di.syncModule
import org.koin.dsl.module

actual val coreModule = module {
    includes(platformModule)

    single { get<AppDatabase>().songDao() }
    single { get<AppDatabase>().setlistDao() }
    single { get<AppDatabase>().syncActionDao() }
    single { get<AppDatabase>().syncDao() }

    includes(supabaseModule)
    includes(syncModule)
    includes(authModule)
    includes(songModule)
    includes(setlistModule)
    includes(prefsModule)
    includes(queueModule)
}

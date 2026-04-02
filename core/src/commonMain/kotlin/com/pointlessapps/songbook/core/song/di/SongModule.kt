package com.pointlessapps.songbook.core.song.di

import com.pointlessapps.songbook.core.song.PublicLyricsRepository
import com.pointlessapps.songbook.core.song.PublicLyricsRepositoryImpl
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.SongRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val songModule = module {
    singleOf(::SongRepositoryImpl).bind<SongRepository>()
    singleOf(::PublicLyricsRepositoryImpl).bind<PublicLyricsRepository>()
}

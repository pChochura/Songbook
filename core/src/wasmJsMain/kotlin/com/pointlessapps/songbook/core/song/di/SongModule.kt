package com.pointlessapps.songbook.core.song.di

import com.pointlessapps.songbook.core.song.PublicLyricsRepository
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.WasmPublicLyricsRepositoryImpl
import com.pointlessapps.songbook.core.song.WasmSongRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val songModule = module {
    singleOf(::WasmSongRepository).bind<SongRepository>()
    singleOf(::WasmPublicLyricsRepositoryImpl).bind<PublicLyricsRepository>()
}

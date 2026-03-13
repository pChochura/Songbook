package com.pointlessapps.songbook.di

object KoinHelper {
    fun initKoin() {
        com.pointlessapps.songbook.di.initKoin(platformModules = listOf(iosDatabaseModule))
    }
}

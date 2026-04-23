package com.pointlessapps.songbook.core.queue.di

import com.pointlessapps.songbook.core.queue.QueueManager
import com.pointlessapps.songbook.core.queue.QueueManagerImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val queueModule = module {
    singleOf(::QueueManagerImpl).bind<QueueManager>()
}

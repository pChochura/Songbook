package com.pointlessapps.songbook.core.network

import com.pointlessapps.songbook.core.network.model.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    val networkStatus: Flow<NetworkStatus>
}

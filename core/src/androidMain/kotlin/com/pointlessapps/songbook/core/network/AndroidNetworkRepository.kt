package com.pointlessapps.songbook.core.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import com.pointlessapps.songbook.core.network.model.NetworkStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

internal class AndroidNetworkRepository(
    private val context: Context,
) : NetworkRepository {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val connectivityManager =
            context.getSystemService<ConnectivityManager>() ?: return@callbackFlow

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.ONLINE)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.OFFLINE)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    trySend(NetworkStatus.ONLINE)
                } else {
                    trySend(NetworkStatus.OFFLINE)
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.onStart {
        emit(getCurrentNetworkStatus())
    }.distinctUntilChanged()

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun getCurrentNetworkStatus(): NetworkStatus {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return NetworkStatus.OFFLINE
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return NetworkStatus.OFFLINE
        return if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            NetworkStatus.ONLINE
        } else {
            NetworkStatus.OFFLINE
        }
    }
}

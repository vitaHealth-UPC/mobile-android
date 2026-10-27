package com.vitahealth.tata.preferences.infrastructure.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Observes validated default connectivity without tying recovery to an Activity. */
internal class ValidatedNetworkObserver(context: Context) {
    private val manager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    fun observe(): Flow<Boolean> =
        callbackFlow {
                fun usable(capabilities: NetworkCapabilities?) =
                    capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ==
                        true
                val callback =
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onCapabilitiesChanged(
                            network: Network,
                            capabilities: NetworkCapabilities,
                        ) {
                            trySend(usable(capabilities))
                        }

                        override fun onLost(network: Network) {
                            trySend(false)
                        }
                    }
                if (manager == null) {
                    trySend(false)
                    close()
                } else {
                    manager.registerDefaultNetworkCallback(callback)
                    trySend(usable(manager.getNetworkCapabilities(manager.activeNetwork)))
                    awaitClose { manager.unregisterNetworkCallback(callback) }
                }
            }
            .distinctUntilChanged()
}

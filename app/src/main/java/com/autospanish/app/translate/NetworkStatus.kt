package com.autospanish.app.translate

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext

object NetworkStatus {
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

@Composable
fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = NetworkStatus.isOnline(context)) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                value = NetworkStatus.isOnline(context)
            }
            override fun onLost(network: Network) {
                value = NetworkStatus.isOnline(context)
            }
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                value = NetworkStatus.isOnline(context)
            }
        }
        cm.registerDefaultNetworkCallback(callback)
        awaitDispose { cm.unregisterNetworkCallback(callback) }
    }
}

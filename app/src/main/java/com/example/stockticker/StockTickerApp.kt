package com.example.stockticker

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.stockticker.data.source.StockDataSource

class StockTickerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val json = assets.open("stocks.json").bufferedReader().use { it.readText() }
        StockDataSource.initialize(json)

        observeAppLifecycle()
        observeNetwork()
    }

    private fun observeAppLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                StockDataSource.setAppForeground(true)
            }

            override fun onStop(owner: LifecycleOwner) {
                StockDataSource.setAppForeground(false)
            }
        })
    }

    private fun observeNetwork() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                StockDataSource.setNetworkAvailable(true)
            }

            override fun onLost(network: Network) {
                StockDataSource.setNetworkAvailable(false)
            }
        })
    }
}

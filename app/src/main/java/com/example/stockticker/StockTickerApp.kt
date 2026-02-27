package com.example.stockticker

import android.app.Application
import com.example.stockticker.data.source.StockDataSource

class StockTickerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val json = assets.open("stocks.json").bufferedReader().use { it.readText() }
        StockDataSource.initialize(json)
    }
}

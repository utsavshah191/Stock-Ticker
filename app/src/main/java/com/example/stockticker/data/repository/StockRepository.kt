package com.example.stockticker.data.repository

import com.example.stockticker.data.model.StockPrice
import com.example.stockticker.data.source.StockDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StockRepository {

    val prices: SharedFlow<List<StockPrice>> = StockDataSource.priceFlow

    val isNetworkAvailable: StateFlow<Boolean> = StockDataSource.networkAvailable

    private val _isFeedActive = MutableStateFlow(false)
    val isFeedActive: StateFlow<Boolean> = _isFeedActive.asStateFlow()

    fun setFeedActive(active: Boolean) {
        _isFeedActive.value = active
    }
}

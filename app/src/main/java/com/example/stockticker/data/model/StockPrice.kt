package com.example.stockticker.data.model

data class StockPrice(
    val symbol: String,
    val price: Double,
    val previousPrice: Double
) {
    val change: Double get() = price - previousPrice
    val isUp: Boolean get() = change >= 0
}

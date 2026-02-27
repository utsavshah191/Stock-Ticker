package com.example.stockticker.data.model

/** Live price update for a single symbol, enriched with static info. */
data class StockPrice(
    val symbol: String,
    val companyName: String,
    val description: String,
    val price: Double,
    val previousPrice: Double
) {
    val change: Double get() = price - previousPrice
    val isUp: Boolean get() = change >= 0
}

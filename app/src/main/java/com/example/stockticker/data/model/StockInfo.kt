package com.example.stockticker.data.model

/** Static stock metadata loaded once from stocks.json. */
data class StockInfo(
    val symbol: String,
    val companyName: String,
    val initialPrice: Double,
    val description: String
)

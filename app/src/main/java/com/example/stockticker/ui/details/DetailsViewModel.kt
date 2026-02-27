package com.example.stockticker.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DetailsUiState(
    val symbol: String = "",
    val price: Double = 0.0,
    val previousPrice: Double = 0.0
)
// Static data for the price ticker
private val staticPrices = mapOf(
    "AAPL"  to Pair(178.50, 177.80),
    "GOOGL" to Pair(141.20, 142.10),
    "MSFT"  to Pair(378.90, 376.50),
    "AMZN"  to Pair(186.40, 184.75),
    "TSLA"  to Pair(245.30, 248.90),
    "META"  to Pair(512.80, 515.30),
    "NVDA"  to Pair(875.60, 868.20),
    "NFLX"  to Pair(634.20, 630.10)
)

class DetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val symbol: String = checkNotNull(savedStateHandle["symbol"])

    private val prices = staticPrices[symbol] ?: Pair(0.0, 0.0)

    private val _uiState = MutableStateFlow(
        DetailsUiState(
            symbol = symbol,
            price = prices.first,
            previousPrice = prices.second
        )
    )
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()
}

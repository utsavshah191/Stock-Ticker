package com.example.stockticker.ui.feed

import androidx.lifecycle.ViewModel
import com.example.stockticker.data.model.StockPrice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FeedUiState(
    val stocks: List<StockPrice> = emptyList(),
    val isConnected: Boolean = false,
    val isFeedActive: Boolean = false
)

private val staticStocks = listOf(
    StockPrice("NVDA",  875.60, 868.20),
    StockPrice("META",  512.80, 515.30),
    StockPrice("NFLX",  634.20, 630.10),
    StockPrice("MSFT",  378.90, 376.50),
    StockPrice("TSLA",  245.30, 248.90),
    StockPrice("AMZN",  186.40, 184.75),
    StockPrice("AAPL",  178.50, 177.80),
    StockPrice("GOOGL", 141.20, 142.10)
)

class FeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        FeedUiState(
            stocks = staticStocks.sortedByDescending { it.price },
            isConnected = true,
            isFeedActive = true
        )
    )
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    fun toggleFeed() {
        _uiState.update {
            it.copy(isFeedActive = !it.isFeedActive, isConnected = !it.isConnected)
        }
    }
}

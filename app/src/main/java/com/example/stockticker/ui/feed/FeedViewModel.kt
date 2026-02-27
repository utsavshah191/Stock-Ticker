package com.example.stockticker.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockticker.data.model.StockPrice
import com.example.stockticker.data.repository.StockRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val stocks: List<StockPrice> = emptyList(),
    val isConnected: Boolean = false,
    val isFeedActive: Boolean = false,
    val isNetworkAvailable: Boolean = false
)

class FeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var feedJob: Job? = null

    init {
        startFeed()
        viewModelScope.launch {
            StockRepository.isNetworkAvailable.collect { available ->
                _uiState.update { state ->
                    state.copy(
                        isNetworkAvailable = available,
                        isConnected = available && state.isFeedActive
                    )
                }
            }
        }
    }

    fun toggleFeed() {
        if (_uiState.value.isFeedActive) stopFeed() else startFeed()
    }

    private fun startFeed() {
        StockRepository.setFeedActive(true)
        feedJob = viewModelScope.launch {
            _uiState.update { it.copy(isFeedActive = true, isConnected = StockRepository.isNetworkAvailable.value) }
            StockRepository.prices.collect { stocks ->
                _uiState.update { state ->
                    state.copy(stocks = stocks.sortedByDescending { it.price })
                }
            }
        }
    }

    private fun stopFeed() {
        feedJob?.cancel()
        feedJob = null
        StockRepository.setFeedActive(false)
        _uiState.update { it.copy(isFeedActive = false, isConnected = false) }
    }
}

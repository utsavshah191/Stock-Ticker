package com.example.stockticker.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockticker.data.repository.StockRepository
import com.example.stockticker.data.source.StockDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsUiState(
    val symbol: String = "",
    val companyName: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val previousPrice: Double = 0.0,
    val isLoading: Boolean = true,
    val isNetworkAvailable: Boolean = false,
    val isSymbolNotFound: Boolean = false
)

class DetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val symbol: String = checkNotNull(savedStateHandle["symbol"])

    private val _uiState = MutableStateFlow(DetailsUiState(symbol = symbol))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        if (symbol !in StockDataSource.symbols) {
            _uiState.update { it.copy(isSymbolNotFound = true, isLoading = false) }
        } else {
            viewModelScope.launch {
                StockRepository.isNetworkAvailable.collect { available ->
                    _uiState.update { it.copy(isNetworkAvailable = available) }
                }
            }
            viewModelScope.launch {
                StockRepository.prices.collect { stocks ->
                    val stock = stocks.find { it.symbol == symbol } ?: return@collect
                    _uiState.update {
                        it.copy(
                            companyName   = stock.companyName,
                            description   = stock.description,
                            price         = stock.price,
                            previousPrice = stock.previousPrice,
                            isLoading     = false
                        )
                    }
                }
            }
        }
    }
}

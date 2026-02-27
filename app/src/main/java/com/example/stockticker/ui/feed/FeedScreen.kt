package com.example.stockticker.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockticker.R
import com.example.stockticker.data.model.StockPrice
import com.example.stockticker.ui.theme.PriceDown
import com.example.stockticker.ui.theme.PriceUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onSymbolClick: (String) -> Unit,
    viewModel: FeedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = if (uiState.isConnected) PriceUp else PriceDown,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isConnected) {
                                stringResource(id = R.string.feed_connected)
                            } else {
                                stringResource(id = R.string.feed_disconnected)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.toggleFeed() }) {
                        Text(
                            text = if (uiState.isFeedActive) {
                                stringResource(id = R.string.feed_stop)
                            } else {
                                stringResource(id = R.string.feed_start)
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!uiState.isNetworkAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.feed_no_internet),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (uiState.stocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.feed_no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(uiState.stocks, key = { it.symbol }) { stock ->
                    StockRow(stock = stock, onClick = { onSymbolClick(stock.symbol) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun StockRow(stock: StockPrice, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stock.symbol,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$${"%.2f".format(stock.price)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(12.dp))

        val arrow = if (stock.isUp) "↑" else "↓"
        val changeColor = if (stock.isUp) PriceUp else PriceDown
        Text(
            text = "$arrow ${"%.2f".format(stock.change)}",
            color = changeColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

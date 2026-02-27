package com.example.stockticker.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.stockticker.ui.theme.PriceDown
import com.example.stockticker.ui.theme.PriceUp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private val symbolDescriptions = mapOf(
    "AAPL" to "Apple Inc. designs, manufactures, and markets consumer electronics, software, and services. Known for the iPhone, Mac, iPad, and the App Store ecosystem.",
    "GOOGL" to "Alphabet Inc. is the parent company of Google. It operates in search, advertising, cloud computing, AI research, and hardware.",
    "MSFT" to "Microsoft Corp. offers software (Windows, Office), cloud services (Azure), gaming (Xbox), and enterprise solutions worldwide.",
    "AMZN" to "Amazon.com Inc. is a global leader in e-commerce, cloud computing (AWS), digital streaming, and artificial intelligence.",
    "TSLA" to "Tesla Inc. designs and manufactures electric vehicles, energy storage systems, and solar products for a sustainable future.",
    "META" to "Meta Platforms Inc. operates Facebook, Instagram, and WhatsApp. It is heavily investing in virtual and augmented reality.",
    "NVDA" to "NVIDIA Corp. is a leader in GPU design for gaming, professional visualization, data centers, and AI computing.",
    "NFLX" to "Netflix Inc. is the world's leading streaming entertainment service with a vast library of TV series, films, and documentaries."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymbolDetailsScreen(
    onBack: () -> Unit,
    viewModel: DetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.symbol, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Arrow"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            PriceCard(uiState = uiState)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = symbolDescriptions[uiState.symbol] ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun PriceCard(uiState: DetailsUiState) {
    val isUp = uiState.price >= uiState.previousPrice
    val change = uiState.price - uiState.previousPrice
    val changeColor = if (isUp) PriceUp else PriceDown

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current Price",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$${"%.2f".format(uiState.price)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isUp) "↑" else "↓",
                    color = changeColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${"%.2f".format(change)} ",
                color = changeColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

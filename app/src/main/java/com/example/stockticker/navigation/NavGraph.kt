package com.example.stockticker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stockticker.ui.details.SymbolDetailsScreen
import com.example.stockticker.ui.feed.FeedScreen

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "feed",
        modifier = modifier
    ) {
        composable("feed") {
            FeedScreen(
                onSymbolClick = { symbol -> navController.navigate("symbol_details/$symbol") }
            )
        }
        composable("symbol_details/{symbol}") {
            SymbolDetailsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

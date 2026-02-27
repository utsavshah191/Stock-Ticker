package com.example.stockticker.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.stockticker.ui.details.SymbolDetailsScreen
import com.example.stockticker.ui.feed.FeedScreen

@Composable
fun NavGraph(modifier: Modifier = Modifier, newIntent: Intent? = null) {
    val navController = rememberNavController()

    // Forward deep links that arrive while the activity is already running (onNewIntent).
    // The NavHost handles the initial deep link from the activity's intent automatically.
    LaunchedEffect(newIntent) {
        newIntent?.let { navController.handleDeepLink(it) }
    }

    NavHost(
        navController    = navController,
        startDestination = "feed",
        modifier         = modifier
    ) {
        composable("feed") {
            FeedScreen(
                onSymbolClick = { symbol -> navController.navigate("symbol_details/$symbol") }
            )
        }
        composable(
            route      = "symbol_details/{symbol}",
            deepLinks  = listOf(navDeepLink { uriPattern = "stocks://symbol/{symbol}" })
        ) {
            SymbolDetailsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

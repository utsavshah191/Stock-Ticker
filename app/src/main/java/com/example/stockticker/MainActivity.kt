package com.example.stockticker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.stockticker.navigation.NavGraph
import com.example.stockticker.ui.splash.SplashScreen
import com.example.stockticker.ui.theme.StockTickerTheme

class MainActivity : ComponentActivity() {


    private val newIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Skip splash when the app is cold-started directly from a deep link.
        val isDeepLink = intent.action == Intent.ACTION_VIEW

        setContent {
            StockTickerTheme {
                var showSplash by remember { mutableStateOf(!isDeepLink) }
                val latestNewIntent by newIntent

                if (showSplash) {
                    SplashScreen(onSplashFinished = { showSplash = false })
                } else {
                    NavGraph(
                        modifier  = Modifier.fillMaxSize(),
                        newIntent = latestNewIntent
                    )
                }
            }
        }
    }

    // Called when launchMode="singleTop" reuses this activity for a new deep link.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        newIntent.value = intent
    }
}

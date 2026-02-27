package com.example.stockticker.data.source

import com.example.stockticker.data.model.StockInfo
import com.example.stockticker.data.model.StockPrice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

object StockDataSource {

    private var stockInfos: List<StockInfo> = emptyList()


    // Default false — ConnectivityManager fires onAvailable immediately if network exists.
    private val isNetworkAvailable = AtomicBoolean(false)
    private val isAppForeground = AtomicBoolean(true)

    private val _networkAvailable = MutableStateFlow(false)
    val networkAvailable: StateFlow<Boolean> = _networkAvailable.asStateFlow()

    // Current WebSocket and request/listener references for reconnection.
    @Volatile
    private var webSocket: WebSocket? = null

    @Volatile
    private var webSocketRequest: Request? = null

    @Volatile
    private var webSocketListener: WebSocketListener? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Called once from [StockTickerApp] before any subscriber can attach.
     * Parses stocks.json and stores the static stock metadata.
     */
    fun initialize(json: String) {
        val array = JSONArray(json)
        stockInfos = buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    StockInfo(
                        symbol       = obj.getString("symbol"),
                        companyName  = obj.getString("companyName"),
                        initialPrice = obj.getDouble("initialPrice"),
                        description  = obj.getString("description")
                    )
                )
            }
        }
    }

    fun setNetworkAvailable(available: Boolean) {
        isNetworkAvailable.set(available)
        _networkAvailable.value = available
        if (!available) {
            // Close any active socket when network is lost.
            webSocket?.close(1001, "Network lost")
            webSocket = null
        } else {
            // Try to reconnect if we are in foreground.
            if (isAppForeground.get()) {
                reconnectIfPossible()
            }
        }
    }

    fun setAppForeground(foreground: Boolean) {
        isAppForeground.set(foreground)
        if (!foreground) {
            // App in background – close the socket to save resources.
            webSocket?.close(1001, "App in background")
            webSocket = null
        } else {
            // Coming back to foreground with network – reconnect.
            if (isNetworkAvailable.get()) {
                reconnectIfPossible()
            }
        }
    }

    /**
     * Single shared WebSocket flow — both Feed and Details observe the same stream.
     * Lazily starts on first subscriber; replays the last emission to new subscribers.
     */
    val priceFlow: SharedFlow<List<StockPrice>> by lazy {
        callbackFlow {
            val infoMap  = stockInfos.associateBy { it.symbol }
            val current  = ConcurrentHashMap<String, Double>(stockInfos.associate { it.symbol to it.initialPrice })
            val previous = ConcurrentHashMap<String, Double>(stockInfos.associate { it.symbol to it.initialPrice })

            val request = Request.Builder()
                .url("wss://ws.postman-echo.com/raw")
                .build()
            webSocketRequest = request

            val listener = object : WebSocketListener() {

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val json       = JSONObject(text)
                        val symbol     = json.getString("symbol")
                        val price      = json.getDouble("price")
                        current[symbol] = price
                        trySend(buildStockList(infoMap, current, previous))
                    } catch (_: Exception) { /* ignore malformed frames */ }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    this@StockDataSource.webSocket = null
                    // Do NOT call close(t) — that permanently kills the SharedFlow.
                    // The send loop is null-safe; reconnection happens via
                    // setNetworkAvailable(true) or setAppForeground(true).
                    if (isAppForeground.get() && isNetworkAvailable.get()) {
                        reconnectIfPossible()
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    this@StockDataSource.webSocket = null
                    // Do NOT call close() — that permanently completes the SharedFlow.
                    // Only reconnect if the close was not triggered by us (code 1000/1001).
                    if (isAppForeground.get() && isNetworkAvailable.get() && code != 1000 && code != 1001) {
                        reconnectIfPossible()
                    }
                }
            }
            webSocketListener = listener

            // Only create the socket if allowed by app + network state.
            if (isAppForeground.get() && isNetworkAvailable.get()) {
                webSocket = client.newWebSocket(request, listener)
            }

            /**
             * Every 2 seconds generate a random price for each symbol,
             * send it to the WebSocket server.
             */
            launch {
                while (isActive) {
                    delay(2_000L)
                    stockInfos.forEach { info ->
                        val cur      = current[info.symbol] ?: info.initialPrice
                        val difference    = (Random.nextDouble() - 0.48) * cur * 0.005
                        val newPrice = (cur + difference).coerceAtLeast(1.0)

                        previous[info.symbol] = cur   // snapshot before echo arrives

                        val message = JSONObject()
                            .put("symbol",      info.symbol)
                            .put("companyName", info.companyName)
                            .put("price",       "%.2f".format(newPrice).toDouble())
                            .toString()

                        webSocket?.send(message)
                        delay(10L)
                    }
                }
            }

            awaitClose {
                webSocket?.close(1000, "Flow cancelled")
                webSocket = null
            }

        }.shareIn(
            scope   = scope,
            started = SharingStarted.Lazily,
            replay  = 1
        )
    }

    private fun reconnectIfPossible() {
        val req = webSocketRequest
        val listener = webSocketListener
        if (req != null && listener != null && webSocket == null) {
            webSocket = client.newWebSocket(req, listener)
        }
    }

    private fun buildStockList(
        infoMap:  Map<String, StockInfo>,
        current:  Map<String, Double>,
        previous: Map<String, Double>
    ): List<StockPrice> = stockInfos.map { info ->
        StockPrice(
            symbol        = info.symbol,
            companyName   = info.companyName,
            description   = info.description,
            price         = current[info.symbol]  ?: info.initialPrice,
            previousPrice = previous[info.symbol] ?: info.initialPrice
        )
    }
}

package com.example.stockticker.data.source

import com.example.stockticker.data.model.StockInfo
import com.example.stockticker.data.model.StockPrice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
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
import kotlin.random.Random

object StockDataSource {

    private var stockInfos: List<StockInfo> = emptyList()

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

            val listener = object : WebSocketListener() {
                /**
                 * Receives the echoed message from the server.
                 * Parses symbol + price, updates current prices, emits the full list.
                 */
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
                    close(t)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    close()
                }
            }

            val webSocket = client.newWebSocket(request, listener)

            /**
             * Every 2 seconds generate a random price for each symbol,
             * send it to the WebSocket server, and wait for the echo.
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

                        webSocket.send(message)
                        delay(10L)
                    }
                }
            }

            awaitClose { webSocket.close(1000, "Flow cancelled") }

        }.shareIn(
            scope   = scope,
            started = SharingStarted.Lazily,
            replay  = 1
        )
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

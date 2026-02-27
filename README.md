Stock Ticker Project
====================

### Overview

Stock Ticker is an Android app built with **Jetpack Compose**, **MVVM**, and a **single shared WebSocket** connection that streams **live prices for 25 stock symbols**. It demonstrates:

- Real‑time price updates over WebSocket
- A scrollable, sorted feed of symbols
- A details screen with stock
- Deep link navigation into the details screen

### Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **Architecture**: MVVM 
- **Navigation**: Navigation Compose
- **Realtime**: OkHttp WebSocket (`wss://ws.postman-echo.com/raw`)


### Features

- **Splash Screen**
  - Simple centered splash with app logo and title.

- **Feed Screen**
  - `LazyColumn` of ~25 symbols (AAPL, GOOGL, TSLA, AMZN, MSFT, NVDA, etc.).
  - Each row shows: symbol, current price, and **green ↑ / red ↓** change.
  - List is **sorted by price descending** and re‑orders as prices change.
  - Top bar:
    - Left: connection indicator (**Connected / Disconnected**).
    - Right: **Start / Stop** toggle for the price feed.
  - Tap a row to open the **Symbol Details** screen.

- **Symbol Details Screen**
  - Shows selected symbol, company name, current price, and change indicator.
  - “About” section with description text from JSON.
  - Shares the same WebSocket stream as the feed (no duplicate connections).

- **Deep Link**
  - URI: `stocks://symbol/{symbol}`
  - Example:
    - `stocks://symbol/AAPL`
  - If the symbol is unknown, the details screen shows **“No stock information available”**.








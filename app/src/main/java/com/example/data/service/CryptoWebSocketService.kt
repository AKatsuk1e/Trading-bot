package com.example.data.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.data.network.CryptoWebSocketManager

/**
 * Android Service layer hosting the background WebSocket connection
 * for streaming real-time exchange ticks to the autonomous bot engine and UI.
 */
class CryptoWebSocketService : Service() {

    private val binder = LocalBinder()
    private val tag = "CryptoWsService"

    inner class LocalBinder : Binder() {
        fun getService(): CryptoWebSocketService = this@CryptoWebSocketService
        fun getWebSocketManager(): CryptoWebSocketManager = webSocketManager
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(tag, "CryptoWebSocketService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val symbols = intent?.getStringArrayListExtra(EXTRA_SYMBOLS) ?: arrayListOf(
            "BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT",
            "XRPUSDT", "DOGEUSDT", "ADAUSDT", "AVAXUSDT"
        )

        when (action) {
            ACTION_START -> {
                Log.i(tag, "Starting WebSocket stream for symbols: $symbols")
                webSocketManager.start(symbols)
            }
            ACTION_STOP -> {
                Log.i(tag, "Stopping WebSocket stream")
                webSocketManager.stop()
                stopSelf()
            }
            else -> {
                webSocketManager.start(symbols)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "CryptoWebSocketService destroyed")
        webSocketManager.stop()
    }

    companion object {
        const val ACTION_START = "com.example.action.START_WS"
        const val ACTION_STOP = "com.example.action.STOP_WS"
        const val EXTRA_SYMBOLS = "extra_symbols"

        // Singleton manager accessible across service, repository, and bot engine
        val webSocketManager: CryptoWebSocketManager by lazy {
            CryptoWebSocketManager()
        }

        fun startStreaming(context: Context, symbols: ArrayList<String>? = null) {
            val intent = Intent(context, CryptoWebSocketService::class.java).apply {
                action = ACTION_START
                if (symbols != null) {
                    putStringArrayListExtra(EXTRA_SYMBOLS, symbols)
                }
            }
            context.startService(intent)
        }

        fun stopStreaming(context: Context) {
            val intent = Intent(context, CryptoWebSocketService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}

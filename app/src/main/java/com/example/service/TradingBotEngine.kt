package com.example.service

import com.example.data.repository.TradingRepository
import com.example.model.TradeSide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TradingBotEngine(
    private val repository: TradingRepository,
    private val scope: CoroutineScope
) {
    private var botJob: Job? = null
    private var fastTickJob: Job? = null
    var isRunning: Boolean = false
        private set

    fun start(geminiKey: String? = null) {
        if (isRunning) return
        isRunning = true
        repository.addLog("AUTONOMOUS_TRIGGER", "AUTONOMOUS TRADING BOT ACTIVATED! Commencing WebSocket-streamed fast decision cycles with 1/4 Kelly Criterion...", "SUCCESS")

        // Fast WebSocket tick listener for sub-second position surveillance & rapid breakout detection
        fastTickJob = scope.launch(Dispatchers.IO) {
            repository.tickFlow.collect { tick ->
                if (!isRunning) return@collect
                // Real-time evaluation of Stop-Loss (-2%) and Take-Profit (+5%) with millisecond execution
                repository.evaluateFastTickStopLossTakeProfit(tick)
            }
        }

        botJob = scope.launch(Dispatchers.IO) {
            var cycleCount = 1
            while (isActive && isRunning) {
                try {
                    repository.addLog("CYCLE_START", "--- Starting Autonomous Scan Cycle #$cycleCount ---", "INFO")

                    // Step 1: Refresh live tickers and candles
                    repository.refreshMarketData()

                    // Step 2: Pick active symbols to analyze
                    val config = repository.botConfig.value
                    val symbolsToScan = config.selectedSymbols

                    for (symbol in symbolsToScan) {
                        if (!isRunning || !isActive) break

                        // Run multi-agent pipeline
                        val result = repository.runAgentAnalysis(symbol, geminiKey)
                        if (result != null) {
                            val isAuthorized = result.portfolioReport.metrics["Execution_Authorized"]?.toBoolean() ?: false
                            val actionStr = result.traderReport.metrics["Action"] ?: "HOLD"
                            val posUsd = result.traderReport.metrics["Position_Usd"]?.replace("$", "")?.toDoubleOrNull() ?: 0.0

                            if (isAuthorized && (actionStr == "BUY" || actionStr == "SELL") && posUsd > 10.0) {
                                val ticker = repository.tickers.value.find { it.symbol == symbol }
                                val currentPrice = ticker?.price ?: 0.0

                                // Check if we already have an open position in this symbol to avoid duplicate over-exposure
                                val currentOpen = repository.portfolio.value.openPositions
                                val hasOpenInSymbol = currentOpen.any { it.symbol == symbol }

                                if (!hasOpenInSymbol && currentPrice > 0.0) {
                                    val side = if (actionStr == "BUY") TradeSide.BUY else TradeSide.SELL
                                    repository.addLog("AUTO_EXECUTE", "Triggering autonomous $actionStr for $symbol: $${String.format("%.2f", posUsd)} (1/4 Kelly size)", "SUCCESS")
                                    repository.executeTrade(
                                        symbol = symbol,
                                        side = side,
                                        usdValue = posUsd,
                                        price = currentPrice,
                                        rationale = "Autonomous Bot: 1/4 Kelly (${result.traderReport.metrics["Kelly_Quarter_Pct"]}) | Edge verified by 4 Agents"
                                    )
                                } else {
                                    repository.addLog("CYCLE_INFO", "Position already active for $symbol. Holding existing allocation.", "INFO")
                                }
                            }
                        }

                        delay(1200) // gentle spacing between symbols in the cycle
                    }

                    cycleCount++
                    val interval = repository.botConfig.value.scanIntervalSeconds
                    repository.addLog("CYCLE_WAIT", "Cycle complete. Next autonomous scan in ${interval}s...", "INFO")
                    delay(interval * 1000L)
                } catch (e: Exception) {
                    repository.addLog("ENGINE_ERROR", "Autonomous cycle warning: ${e.message}", "WARN")
                    delay(5000L)
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        fastTickJob?.cancel()
        fastTickJob = null
        botJob?.cancel()
        botJob = null
        repository.addLog("AUTONOMOUS_TRIGGER", "AUTONOMOUS TRADING BOT PAUSED by user trigger.", "WARN")
    }
}

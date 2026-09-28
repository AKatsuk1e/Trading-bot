package com.example.data.repository

import android.content.Context
import com.example.agent.AnalysisContext
import com.example.agent.FullAnalysisResult
import com.example.agent.MultiAgentOrchestrator
import com.example.data.local.AppDatabase
import com.example.data.local.SignalEntity
import com.example.data.local.TradeEntity
import com.example.data.network.CryptoWebSocketManager
import com.example.data.network.MarketDataService
import com.example.data.network.NewsDataService
import com.example.data.service.CryptoWebSocketService
import com.example.model.AutoBotConfig
import com.example.model.BotLogMessage
import com.example.model.Candle
import com.example.model.CryptoTicker
import com.example.model.MarketNewsItem
import com.example.model.OrderBookDepth
import com.example.model.PortfolioSummary
import com.example.model.RealTimeTick
import com.example.model.Trade
import com.example.model.TradeSide
import com.example.model.TradeStatus
import com.example.model.TradingSignal
import com.example.model.WsMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.max

class TradingRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val tradeDao = db.tradeDao()
    private val signalDao = db.signalDao()

    private val marketService = MarketDataService()
    private val newsService = NewsDataService()
    private val orchestrator = MultiAgentOrchestrator()
    private val repoScope = CoroutineScope(Dispatchers.IO)

    // WebSocket Manager for ultra-fast exchange tick streaming
    val wsManager: CryptoWebSocketManager = CryptoWebSocketService.webSocketManager
    val wsMetrics: StateFlow<WsMetrics> = wsManager.connectionMetrics
    val tickFlow: SharedFlow<RealTimeTick> = wsManager.tickFlow
    val realTimeTicks: StateFlow<Map<String, RealTimeTick>> = wsManager.realTimeTicks

    // Live StateFlows
    private val _tickers = MutableStateFlow<List<CryptoTicker>>(emptyList())
    val tickers = _tickers.asStateFlow()

    private val _selectedSymbol = MutableStateFlow("BTCUSDT")
    val selectedSymbol = _selectedSymbol.asStateFlow()

    private val _currentCandles = MutableStateFlow<List<Candle>>(emptyList())
    val currentCandles = _currentCandles.asStateFlow()

    private val _currentDepth = MutableStateFlow<OrderBookDepth?>(null)
    val currentDepth = _currentDepth.asStateFlow()

    private val _news = MutableStateFlow<List<MarketNewsItem>>(emptyList())
    val news = _news.asStateFlow()

    private val _latestAnalysis = MutableStateFlow<FullAnalysisResult?>(null)
    val latestAnalysis = _latestAnalysis.asStateFlow()

    private val _botLogs = MutableStateFlow<List<BotLogMessage>>(emptyList())
    val botLogs = _botLogs.asStateFlow()

    private val _botConfig = MutableStateFlow(AutoBotConfig())
    val botConfig = _botConfig.asStateFlow()

    private val _portfolio = MutableStateFlow(PortfolioSummary())
    val portfolio = _portfolio.asStateFlow()

    // Room Flows
    val allTrades: Flow<List<Trade>> = tradeDao.getAllTrades().map { list -> list.map { it.toDomain() } }
    val openTrades: Flow<List<Trade>> = tradeDao.getOpenTrades().map { list -> list.map { it.toDomain() } }
    val closedTrades: Flow<List<Trade>> = tradeDao.getClosedTrades().map { list -> list.map { it.toDomain() } }
    val signals: Flow<List<TradingSignal>> = signalDao.getAllSignals().map { list -> list.map { it.toDomain() } }

    init {
        // Automatically start the WebSocket streaming for high-speed tick updates
        wsManager.start()

        // Stream real-time ticks to update market prices and trigger stop-loss/take-profit in milliseconds
        repoScope.launch {
            tickFlow.collect { tick ->
                updateTickersFromRealTimeTick(tick)
                evaluateFastTickStopLossTakeProfit(tick)
            }
        }
    }

    /**
     * Millisecond fast evaluation of stop-loss and take-profit triggered immediately
     * when real-time exchange ticks stream in over WebSocket.
     */
    suspend fun evaluateFastTickStopLossTakeProfit(tick: RealTimeTick) {
        val openPositions = _portfolio.value.openPositions
        val matchingPositions = openPositions.filter { it.symbol == tick.symbol }

        for (pos in matchingPositions) {
            val price = tick.price
            if (pos.side == TradeSide.BUY) {
                // Take-Profit check
                if (price >= pos.takeProfitPrice && pos.takeProfitPrice > 0.0) {
                    addLog("WS_FAST_TP", "⚡ Millisecond Take-Profit executed for ${pos.symbol} @ $${String.format("%.2f", price)} (Target: $${String.format("%.2f", pos.takeProfitPrice)})", "SUCCESS")
                    closeTrade(pos.id, price, TradeStatus.CLOSED_TP)
                }
                // Stop-Loss check
                else if (price <= pos.stopLossPrice && pos.stopLossPrice > 0.0) {
                    addLog("WS_FAST_SL", "⚡ Millisecond Stop-Loss executed for ${pos.symbol} @ $${String.format("%.2f", price)} (Floor: $${String.format("%.2f", pos.stopLossPrice)})", "DANGER")
                    closeTrade(pos.id, price, TradeStatus.CLOSED_SL)
                }
            } else if (pos.side == TradeSide.SELL) {
                // Short TP check
                if (price <= pos.takeProfitPrice && pos.takeProfitPrice > 0.0) {
                    addLog("WS_FAST_TP", "⚡ Millisecond Take-Profit executed for short ${pos.symbol} @ $${String.format("%.2f", price)}", "SUCCESS")
                    closeTrade(pos.id, price, TradeStatus.CLOSED_TP)
                }
                // Short SL check
                else if (price >= pos.stopLossPrice && pos.stopLossPrice > 0.0) {
                    addLog("WS_FAST_SL", "⚡ Millisecond Stop-Loss executed for short ${pos.symbol} @ $${String.format("%.2f", price)}", "DANGER")
                    closeTrade(pos.id, price, TradeStatus.CLOSED_SL)
                }
            }
        }
    }

    private fun updateTickersFromRealTimeTick(tick: RealTimeTick) {
        val currentList = _tickers.value.toMutableList()
        val index = currentList.indexOfFirst { it.symbol == tick.symbol }
        if (index >= 0) {
            val old = currentList[index]
            val updated = old.copy(
                price = tick.price,
                priceChangePercent24h = if (tick.priceChangePercent24h != 0.0) tick.priceChangePercent24h else old.priceChangePercent24h,
                highPrice24h = if (tick.high24h > old.highPrice24h) tick.high24h else old.highPrice24h,
                lowPrice24h = if (tick.low24h > 0 && (old.lowPrice24h == 0.0 || tick.low24h < old.lowPrice24h)) tick.low24h else old.lowPrice24h,
                lastUpdated = tick.timestamp
            )
            currentList[index] = updated
            _tickers.value = currentList
        }
    }

    fun setSelectedSymbol(symbol: String) {
        _selectedSymbol.value = symbol
    }

    fun updateBotConfig(config: AutoBotConfig) {
        _botConfig.value = config
        addLog("CONFIG", "Bot configuration updated: 1/4 Kelly=${(config.kellyFraction * 100).toInt()}%, Max Risk=${(config.maxPositionPct * 100).toInt()}%", "INFO")
    }

    fun addLog(tag: String, message: String, level: String = "INFO") {
        val newLog = BotLogMessage(tag = tag, message = message, level = level)
        val updated = (listOf(newLog) + _botLogs.value).take(100)
        _botLogs.value = updated
    }

    /**
     * Refreshes all market data: tickers, selected symbol candles, order book, news.
     */
    suspend fun refreshMarketData() {
        try {
            val liveTickers = marketService.fetchLiveTickers()
            if (liveTickers.isNotEmpty()) {
                _tickers.value = liveTickers
            }

            val currentSymbol = _selectedSymbol.value
            val candles = marketService.fetchCandles(currentSymbol)
            _currentCandles.value = candles

            val depth = marketService.fetchOrderBook(currentSymbol)
            _currentDepth.value = depth

            if (_news.value.isEmpty()) {
                val liveNews = newsService.fetchLiveCryptoNews()
                _news.value = liveNews
            }

            // Also check open positions for Stop-Loss or Take-Profit hits
            checkOpenPositionsAgainstPrices(liveTickers)
        } catch (e: Exception) {
            addLog("DATA_ERROR", "Refresh market data notice: ${e.message}", "WARN")
        }
    }

    /**
     * Runs multi-agent AI analysis on the currently selected symbol or a given symbol.
     */
    suspend fun runAgentAnalysis(symbol: String = _selectedSymbol.value, geminiKey: String? = null): FullAnalysisResult? {
        val ticker = _tickers.value.find { it.symbol == symbol } ?: _tickers.value.firstOrNull() ?: return null
        val candles = if (symbol == _selectedSymbol.value && _currentCandles.value.isNotEmpty()) {
            _currentCandles.value
        } else {
            marketService.fetchCandles(symbol)
        }
        val depth = marketService.fetchOrderBook(symbol)
        val newsItems = if (_news.value.isNotEmpty()) _news.value else newsService.fetchLiveCryptoNews()

        addLog("AGENT_TEAM", "Deploying 4 Analyst Agents for $symbol at $${ticker.price}...", "INFO")

        val currentPort = _portfolio.value
        val context = AnalysisContext(
            ticker = ticker,
            candles = candles,
            depth = depth,
            news = newsItems,
            portfolioValue = currentPort.totalEquity,
            currentDailyPnlPct = if (currentPort.totalEquity > 0) currentPort.totalRealizedPnl / currentPort.totalEquity else 0.0,
            currentDrawdownPct = currentPort.maxDrawdownPct,
            openPositionsCount = currentPort.openPositions.size,
            geminiKey = geminiKey
        )

        val result = orchestrator.runPipeline(context)
        _latestAnalysis.value = result

        val traderAction = result.traderReport.metrics["Action"] ?: "HOLD"
        val kellyPct = result.traderReport.metrics["Kelly_Quarter_Pct"] ?: "0.0%"
        val riskPassed = result.riskReport.metrics["Risk_Approved"] ?: "false"

        addLog("DEBATE", "Bull Advocate ${(result.bullReport.confidence * 100).toInt()}% vs Bear Advocate ${(result.bearReport.confidence * 100).toInt()}%", "INFO")
        addLog("KELLY_MATH", "1/4 Kelly Sizing: $kellyPct | Trader Action: $traderAction", if (traderAction == "BUY") "SUCCESS" else "INFO")
        addLog("RISK_AUDIT", "6-Gate Risk Review: ${if (riskPassed == "true") "APPROVED ✓" else "REJECTED ✗"}", if (riskPassed == "true") "SUCCESS" else "WARN")

        // Persist signal to Room
        result.generatedSignal?.let { sig ->
            signalDao.insertSignal(SignalEntity.fromDomain(sig))
            addLog("SIGNAL_SAVED", "High-Edge Signal generated for ${sig.symbol}: ${sig.side} at $${sig.entryPrice}", "SUCCESS")
        }

        return result
    }

    /**
     * Executes a trade either in Paper simulation or Real Exchange.
     */
    suspend fun executeTrade(
        symbol: String,
        side: TradeSide,
        usdValue: Double,
        price: Double,
        rationale: String = "Multi-Agent 1/4 Kelly Execution"
    ): Trade {
        val qty = if (price > 0) usdValue / price else 0.0
        val trade = Trade(
            symbol = symbol,
            side = side,
            entryPrice = price,
            exitPrice = 0.0,
            amount = qty,
            usdValue = usdValue,
            pnl = 0.0,
            pnlPercent = 0.0,
            status = TradeStatus.OPEN,
            entryTime = System.currentTimeMillis(),
            kellyFraction = _botConfig.value.kellyFraction,
            rationale = rationale
        )

        // Save to Room DB
        tradeDao.insertTrade(TradeEntity.fromDomain(trade))

        // Update local portfolio cash & open positions
        val curPort = _portfolio.value
        val newCash = max(0.0, curPort.availableCash - usdValue)
        val updatedOpen = curPort.openPositions + trade
        val totalInPos = updatedOpen.sumOf { it.usdValue }

        _portfolio.value = curPort.copy(
            availableCash = newCash,
            inPositionsValue = totalInPos,
            totalEquity = newCash + totalInPos,
            openPositions = updatedOpen
        )

        addLog("ORDER_FILLED", "FILLED ${side.name} ${String.format("%.4f", qty)} $symbol @ $${String.format("%.2f", price)} ($$usdValue)", "SUCCESS")
        return trade
    }

    /**
     * Closes an open trade manually or automatically when TP or SL is triggered.
     */
    suspend fun closeTrade(tradeId: String, currentPrice: Double, closeReason: TradeStatus) {
        val curPort = _portfolio.value
        val target = curPort.openPositions.find { it.id == tradeId } ?: return

        val pnl = (currentPrice - target.entryPrice) * target.amount
        val pnlPct = if (target.entryPrice > 0) ((currentPrice - target.entryPrice) / target.entryPrice) * 100.0 else 0.0
        val closedTrade = target.copy(
            exitPrice = currentPrice,
            pnl = pnl,
            pnlPercent = pnlPct,
            status = closeReason,
            exitTime = System.currentTimeMillis()
        )

        // Update in Room DB
        tradeDao.updateTrade(TradeEntity.fromDomain(closedTrade))

        val remainingOpen = curPort.openPositions.filter { it.id != tradeId }
        val returnedCash = target.usdValue + pnl
        val newAvailableCash = curPort.availableCash + returnedCash
        val newInPos = remainingOpen.sumOf { it.usdValue }
        val newRealizedPnl = curPort.totalRealizedPnl + pnl

        val isWin = pnl > 0
        val newWins = curPort.winCount + if (isWin) 1 else 0
        val newLoss = curPort.lossCount + if (!isWin) 1 else 0
        val totalFinished = newWins + newLoss
        val winRate = if (totalFinished > 0) (newWins.toDouble() / totalFinished.toDouble()) * 100.0 else 0.0

        _portfolio.value = curPort.copy(
            availableCash = newAvailableCash,
            inPositionsValue = newInPos,
            totalEquity = newAvailableCash + newInPos,
            totalRealizedPnl = newRealizedPnl,
            winCount = newWins,
            lossCount = newLoss,
            winRate = winRate,
            openPositions = remainingOpen
        )

        val logType = if (isWin) "SUCCESS" else "WARN"
        addLog("POSITION_CLOSED", "CLOSED ${closedTrade.symbol} ($closeReason) PnL: ${if (pnl >= 0) "+$" else "-$"}${String.format("%.2f", kotlin.math.abs(pnl))} (${String.format("%.2f", pnlPct)}%)", logType)
    }

    /**
     * Panic button: Closes all active open positions immediately.
     */
    suspend fun closeAllPositions() {
        val openList = _portfolio.value.openPositions.toList()
        for (pos in openList) {
            val ticker = _tickers.value.find { it.symbol == pos.symbol }
            val currentPrice = ticker?.price ?: pos.entryPrice
            closeTrade(pos.id, currentPrice, TradeStatus.CLOSED_MANUAL)
        }
        addLog("PANIC_SELL", "ALL open positions liquidated to cash successfully.", "DANGER")
    }

    /**
     * Resets paper trading portfolio to fresh $10,000 USDT.
     */
    suspend fun resetPortfolio() {
        tradeDao.deleteAllTrades()
        signalDao.deleteAllSignals()
        _portfolio.value = PortfolioSummary(
            totalEquity = 10000.0,
            availableCash = 10000.0,
            inPositionsValue = 0.0,
            totalRealizedPnl = 0.0,
            totalUnrealizedPnl = 0.0,
            winCount = 0,
            lossCount = 0,
            winRate = 0.0,
            maxDrawdownPct = 0.0,
            openPositions = emptyList()
        )
        addLog("PORTFOLIO_RESET", "Paper portfolio reset to $10,000.00 USDT balance.", "INFO")
    }

    /**
     * Checks open positions for Take Profit (+5%) and Stop Loss (-2%).
     */
    private suspend fun checkOpenPositionsAgainstPrices(tickers: List<CryptoTicker>) {
        val openList = _portfolio.value.openPositions
        if (openList.isEmpty()) return

        var unrealizedPnlTotal = 0.0
        val cfg = _botConfig.value

        for (pos in openList) {
            val ticker = tickers.find { it.symbol == pos.symbol } ?: continue
            val currentPrice = ticker.price
            val pnl = (currentPrice - pos.entryPrice) * pos.amount
            val pnlPct = if (pos.entryPrice > 0) (currentPrice - pos.entryPrice) / pos.entryPrice else 0.0
            unrealizedPnlTotal += pnl

            // Auto Take Profit (+5%)
            if (pnlPct >= cfg.takeProfitPct) {
                closeTrade(pos.id, currentPrice, TradeStatus.CLOSED_TP)
            }
            // Auto Stop Loss (-2%)
            else if (pnlPct <= -cfg.stopLossPct) {
                closeTrade(pos.id, currentPrice, TradeStatus.CLOSED_SL)
            }
        }

        val curPort = _portfolio.value
        _portfolio.value = curPort.copy(
            totalUnrealizedPnl = unrealizedPnlTotal,
            totalEquity = curPort.availableCash + curPort.inPositionsValue + unrealizedPnlTotal
        )
    }
}

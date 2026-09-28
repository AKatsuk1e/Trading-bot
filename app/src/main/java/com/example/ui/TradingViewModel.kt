package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.TradingRepository
import com.example.model.AutoBotConfig
import com.example.model.BotLogMessage
import com.example.model.Candle
import com.example.model.CryptoTicker
import com.example.model.MarketNewsItem
import com.example.model.OrderBookDepth
import com.example.model.PortfolioSummary
import com.example.model.Trade
import com.example.model.TradeSide
import com.example.model.TradeStatus
import com.example.model.TradingSignal
import com.example.service.TradingBotEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    val repository = TradingRepository(application)
    private val botEngine = TradingBotEngine(repository, viewModelScope)

    val tickers: StateFlow<List<CryptoTicker>> = repository.tickers
    val selectedSymbol: StateFlow<String> = repository.selectedSymbol
    val candles: StateFlow<List<Candle>> = repository.currentCandles
    val depth: StateFlow<OrderBookDepth?> = repository.currentDepth
    val news: StateFlow<List<MarketNewsItem>> = repository.news
    val latestAnalysis = repository.latestAnalysis
    val botLogs: StateFlow<List<BotLogMessage>> = repository.botLogs
    val botConfig: StateFlow<AutoBotConfig> = repository.botConfig
    val portfolio: StateFlow<PortfolioSummary> = repository.portfolio

    val allTrades: StateFlow<List<Trade>> = repository.allTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openTrades: StateFlow<List<Trade>> = repository.openTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val closedTrades: StateFlow<List<Trade>> = repository.closedTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val signals: StateFlow<List<TradingSignal>> = repository.signals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAutonomousRunning = MutableStateFlow(false)
    val isAutonomousRunning: StateFlow<Boolean> = _isAutonomousRunning.asStateFlow()

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _exchangeApiKey = MutableStateFlow("")
    val exchangeApiKey: StateFlow<String> = _exchangeApiKey.asStateFlow()

    private val _exchangeApiSecret = MutableStateFlow("")
    val exchangeApiSecret: StateFlow<String> = _exchangeApiSecret.asStateFlow()

    val currentTicker: StateFlow<CryptoTicker?> = combine(tickers, selectedSymbol) { list, symbol ->
        list.find { it.symbol == symbol } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Initial market data fetch
        viewModelScope.launch {
            repository.refreshMarketData()
            // Run initial analysis
            repository.runAgentAnalysis()
        }

        // Periodic background price refresh ticker every 5 seconds
        viewModelScope.launch {
            while (isActive) {
                delay(5000L)
                repository.refreshMarketData()
            }
        }
    }

    fun selectSymbol(symbol: String) {
        repository.setSelectedSymbol(symbol)
        viewModelScope.launch {
            repository.refreshMarketData()
            repository.runAgentAnalysis(symbol, _geminiApiKey.value)
        }
    }

    fun toggleAutonomousTrading(enabled: Boolean) {
        _isAutonomousRunning.value = enabled
        repository.updateBotConfig(botConfig.value.copy(isAutoTradingEnabled = enabled))
        if (enabled) {
            botEngine.start(_geminiApiKey.value)
        } else {
            botEngine.stop()
        }
    }

    fun runManualAgentAnalysis() {
        viewModelScope.launch {
            repository.runAgentAnalysis(selectedSymbol.value, _geminiApiKey.value)
        }
    }

    fun executeManualTrade(side: TradeSide, usdValue: Double) {
        viewModelScope.launch {
            val ticker = currentTicker.value ?: return@launch
            repository.executeTrade(
                symbol = ticker.symbol,
                side = side,
                usdValue = usdValue,
                price = ticker.price,
                rationale = "Manual Trader 1/4 Kelly Execution"
            )
        }
    }

    fun closePosition(tradeId: String) {
        viewModelScope.launch {
            val trade = portfolio.value.openPositions.find { it.id == tradeId } ?: return@launch
            val ticker = tickers.value.find { it.symbol == trade.symbol }
            val price = ticker?.price ?: trade.entryPrice
            repository.closeTrade(tradeId, price, TradeStatus.CLOSED_MANUAL)
        }
    }

    fun panicSellAll() {
        viewModelScope.launch {
            repository.closeAllPositions()
        }
    }

    fun resetPortfolio() {
        viewModelScope.launch {
            repository.resetPortfolio()
        }
    }

    fun updateConfig(config: AutoBotConfig) {
        repository.updateBotConfig(config)
    }

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        repository.addLog("API_KEY", "Gemini AI API key updated.", "INFO")
    }

    fun setExchangeCredentials(apiKey: String, secret: String, exchange: String, isReal: Boolean) {
        _exchangeApiKey.value = apiKey
        _exchangeApiSecret.value = secret
        val currentCfg = botConfig.value
        repository.updateBotConfig(currentCfg.copy(
            exchangeName = exchange,
            isRealMoneyMode = isReal
        ))
        val mode = if (isReal) "REAL MONEY ($exchange)" else "PAPER TRADING (Simulation)"
        repository.addLog("EXCHANGE_AUTH", "Trading Mode set to $mode. Credentials secured.", "SUCCESS")
    }
}

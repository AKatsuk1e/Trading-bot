package com.example.model

enum class TradeSide {
    BUY, SELL, HOLD
}

enum class AgentSignal {
    BULLISH, BEARISH, NEUTRAL
}

enum class TradeStatus {
    OPEN,
    CLOSED_TP,
    CLOSED_SL,
    CLOSED_MANUAL
}

data class CryptoTicker(
    val symbol: String,
    val name: String,
    val price: Double,
    val priceChange24h: Double,
    val priceChangePercent24h: Double,
    val highPrice24h: Double,
    val lowPrice24h: Double,
    val volume24h: Double,
    val quoteVolume24h: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class OrderBookDepth(
    val symbol: String,
    val bids: List<Pair<Double, Double>>, // price, quantity
    val asks: List<Pair<Double, Double>>,
    val bidVolumeRatio: Double            // e.g. 0.55 = 55% bids (bullish buy wall)
)

data class AgentReport(
    val agentName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val signal: AgentSignal,
    val confidence: Double,               // 0.0 to 1.0
    val analysis: String,
    val keyPoints: List<String>,
    val dataSources: List<String>,
    val metrics: Map<String, String> = emptyMap()
)

data class TradingSignal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val symbol: String,
    val side: TradeSide,
    val confidence: Double,
    val winProbability: Double,
    val winLossRatio: Double,
    val kellyPercentage: Double,
    val entryPrice: Double,
    val targetPrice: Double,
    val stopLossPrice: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val reasoning: String,
    val timeframe: String = "15m",
    val status: String = "ACTIVE"
)

data class Trade(
    val id: String = java.util.UUID.randomUUID().toString(),
    val symbol: String,
    val side: TradeSide,
    val entryPrice: Double,
    val exitPrice: Double = 0.0,
    val amount: Double,
    val usdValue: Double,
    val pnl: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val status: TradeStatus = TradeStatus.OPEN,
    val entryTime: Long = System.currentTimeMillis(),
    val exitTime: Long? = null,
    val kellyFraction: Double = 0.25,
    val rationale: String = ""
)

data class MarketNewsItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val source: String,
    val url: String = "",
    val timeAgo: String,
    val sentiment: AgentSignal = AgentSignal.NEUTRAL,
    val impact: String = "Medium"
)

data class PortfolioSummary(
    val totalEquity: Double = 10000.0,
    val availableCash: Double = 10000.0,
    val inPositionsValue: Double = 0.0,
    val totalRealizedPnl: Double = 0.0,
    val totalUnrealizedPnl: Double = 0.0,
    val winCount: Int = 0,
    val lossCount: Int = 0,
    val winRate: Double = 0.0,
    val maxDrawdownPct: Double = 0.0,
    val openPositions: List<Trade> = emptyList()
)

data class AutoBotConfig(
    val isAutoTradingEnabled: Boolean = false,
    val scanIntervalSeconds: Int = 15,
    val kellyFraction: Double = 0.25, // 1/4 Kelly
    val maxPositionPct: Double = 0.06, // 6% max
    val stopLossPct: Double = 0.02,    // 2% Stop loss
    val takeProfitPct: Double = 0.05,  // 5% Take profit
    val trailingStopPct: Double = 0.03, // 3% Trailing stop
    val isRealMoneyMode: Boolean = false,
    val exchangeName: String = "Binance",
    val selectedSymbols: List<String> = listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT", "XRPUSDT")
)

data class BotLogMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: String = "INFO" // INFO, SUCCESS, WARN, DANGER
)

enum class WsConnectionStatus {
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    DISCONNECTED,
    ERROR
}

data class RealTimeTick(
    val symbol: String,
    val price: Double,
    val quantity: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val priceChangePercent24h: Double = 0.0,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val volume24h: Double = 0.0,
    val isBuyerMaker: Boolean = false,
    val bestBid: Double = 0.0,
    val bestAsk: Double = 0.0
)

data class WsMetrics(
    val messagesReceived: Long = 0L,
    val lastTickTimestamp: Long = 0L,
    val latencyMs: Long = 0L,
    val activeStreams: List<String> = emptyList(),
    val status: WsConnectionStatus = WsConnectionStatus.DISCONNECTED
)

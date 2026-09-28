package com.example.agent

import com.example.model.AgentReport
import com.example.model.AgentSignal
import com.example.model.Candle
import com.example.model.CryptoTicker
import com.example.model.MarketNewsItem
import com.example.model.OrderBookDepth

data class AnalysisContext(
    val ticker: CryptoTicker,
    val candles: List<Candle>,
    val depth: OrderBookDepth,
    val news: List<MarketNewsItem>,
    val analystReports: List<AgentReport> = emptyList(),
    val bullReport: AgentReport? = null,
    val bearReport: AgentReport? = null,
    val traderReport: AgentReport? = null,
    val riskReport: AgentReport? = null,
    val portfolioValue: Double = 10000.0,
    val currentDailyPnlPct: Double = 0.0,
    val currentDrawdownPct: Double = 0.0,
    val openPositionsCount: Int = 0,
    val geminiKey: String? = null
)

interface BaseAgent {
    val name: String
    suspend fun analyze(symbol: String, context: AnalysisContext): AgentReport
}

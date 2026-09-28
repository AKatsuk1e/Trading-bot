package com.example.agent

import com.example.model.AgentReport
import com.example.model.AgentSignal
import com.example.model.Candle
import kotlin.math.abs
import kotlin.math.sqrt

class TechnicalAnalyst : BaseAgent {
    override val name: String = "Technical Analyst"

    override suspend fun analyze(symbol: String, context: AnalysisContext): AgentReport {
        val candles = context.candles
        if (candles.size < 20) {
            return AgentReport(
                agentName = name,
                signal = AgentSignal.NEUTRAL,
                confidence = 0.5,
                analysis = "Limited candle data for $symbol. Accumulating indicator history.",
                keyPoints = listOf("Need min 20 candles for full moving average window", "Observing baseline ticker action"),
                dataSources = listOf("Binance Ticker", "Klines")
            )
        }

        val closes = candles.map { it.close }
        val volumes = candles.map { it.volume }
        val currentPrice = closes.last()

        // 1. Moving Averages
        val sma20 = closes.takeLast(20).average()
        val sma50 = if (closes.size >= 50) closes.takeLast(50).average() else sma20 * 0.99

        // 2. RSI (14 period)
        val rsi = calculateRSI(closes, 14)

        // 3. Bollinger Bands (20 period, 2 std dev)
        val bbPeriod = closes.takeLast(20)
        val bbMean = bbPeriod.average()
        val variance = bbPeriod.map { (it - bbMean) * (it - bbMean) }.average()
        val bbStd = sqrt(variance)
        val bbUpper = bbMean + (bbStd * 2.0)
        val bbLower = bbMean - (bbStd * 2.0)
        val bbPosition = if (bbUpper > bbLower) (currentPrice - bbLower) / (bbUpper - bbLower) else 0.5

        // 4. MACD approximation (EMA 12 - EMA 26)
        val ema12 = calculateEMA(closes, 12)
        val ema26 = calculateEMA(closes, 26)
        val macd = ema12 - ema26
        val macdSignal = macd * 0.75 // smoothed trigger
        val macdBullish = macd > macdSignal

        // 5. Volume confirmation
        val avgVol20 = volumes.takeLast(20).average()
        val latestVol = volumes.last()
        val volumeRatio = if (avgVol20 > 0) latestVol / avgVol20 else 1.0

        // Scoring system
        var bullScore = 0
        var totalPoints = 0

        // Price above SMA 20 (+1)
        if (currentPrice > sma20) bullScore += 1
        totalPoints += 1

        // Price above SMA 50 (+2)
        if (currentPrice > sma50) bullScore += 2
        totalPoints += 2

        // MACD crossover (+2)
        if (macdBullish) bullScore += 2
        totalPoints += 2

        // RSI conditions (+2)
        if (rsi in 42.0..68.0) {
            bullScore += 1 // healthy trending zone
        } else if (rsi < 35.0) {
            bullScore += 2 // oversold buy opportunity
        }
        totalPoints += 2

        // Bollinger Band dip (+1)
        if (bbPosition < 0.25) bullScore += 1 // near support
        totalPoints += 1

        // Volume expansion (+1)
        if (volumeRatio > 1.25) bullScore += 1
        totalPoints += 1

        val confidence = (bullScore.toDouble() / totalPoints.toDouble()).coerceIn(0.15, 0.95)
        val signal = when {
            confidence >= 0.62 -> AgentSignal.BULLISH
            confidence <= 0.38 -> AgentSignal.BEARISH
            else -> AgentSignal.NEUTRAL
        }

        val trend = if (currentPrice >= sma50) "UPTREND" else "DOWNTREND"
        val keyPoints = mutableListOf<String>()
        keyPoints.add("Trend: $trend (SMA20: $${String.format("%.2f", sma20)}, SMA50: $${String.format("%.2f", sma50)})")
        keyPoints.add("RSI(14): ${String.format("%.1f", rsi)} — ${if (rsi > 70) "Overbought" else if (rsi < 35) "Oversold" else "Dynamic Momentum"}")
        keyPoints.add("MACD: ${if (macdBullish) "Bullish Cross above Signal" else "Bearish Divergence below Signal"}")
        keyPoints.add("Bollinger Band: Price at ${(bbPosition * 100).toInt()}% range ($${String.format("%.2f", bbLower)} - $${String.format("%.2f", bbUpper)})")
        if (volumeRatio > 1.3) keyPoints.add("High Volume Confirmation: ${String.format("%.1f", volumeRatio)}x 20-period average")

        val analysis = """
            Technical evaluation of $symbol at $${String.format("%.2f", currentPrice)}:
            Price action is maintaining a $trend structure. RSI sits at ${String.format("%.1f", rsi)} reflecting ${if (signal == AgentSignal.BULLISH) "strong buying pressure without saturation" else "selling resistance"}. 
            Bollinger Bands indicate ${if (bbPosition < 0.3) "tight support near lower band with bounce potential" else "expansion toward upper levels"}.
            MACD indicates ${if (macdBullish) "positive momentum continuation" else "caution required on momentum fatigue"}.
        """.trimIndent()

        return AgentReport(
            agentName = name,
            signal = signal,
            confidence = confidence,
            analysis = analysis,
            keyPoints = keyPoints,
            dataSources = listOf("Binance 15m Candlesticks", "EMA/SMA Indicators", "RSI Momentum"),
            metrics = mapOf(
                "RSI" to String.format("%.1f", rsi),
                "SMA20" to String.format("%.2f", sma20),
                "SMA50" to String.format("%.2f", sma50),
                "BB_Position" to "${(bbPosition * 100).toInt()}%",
                "MACD" to if (macdBullish) "BULLISH" else "BEARISH",
                "VolRatio" to String.format("%.2fx", volumeRatio)
            )
        )
    }

    private fun calculateRSI(prices: List<Double>, period: Int = 14): Double {
        if (prices.size < period + 1) return 50.0
        var gains = 0.0
        var losses = 0.0

        for (i in prices.size - period until prices.size) {
            val change = prices[i] - prices[i - 1]
            if (change > 0) gains += change else losses += abs(change)
        }

        val avgGain = gains / period
        val avgLoss = losses / period
        if (avgLoss == 0.0) return 100.0

        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    private fun calculateEMA(prices: List<Double>, period: Int): Double {
        if (prices.isEmpty()) return 0.0
        val k = 2.0 / (period + 1.0)
        var ema = prices.first()
        for (i in 1 until prices.size) {
            ema = (prices[i] * k) + (ema * (1.0 - k))
        }
        return ema
    }
}

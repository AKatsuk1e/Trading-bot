package com.example.core.kelly

import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

data class KellyResult(
    val fullKellyFraction: Double,
    val kellyPercentage: Double,       // Fractional Kelly % (e.g. 1/4 Kelly)
    val quarterKelly: Double,          // 1/4 Kelly fraction
    val halfKelly: Double,             // 1/2 Kelly fraction
    val expectedGrowth: Double,        // Expected logarithmic growth
    val confidenceScore: Double,       // Confidence in the estimate (0.0 - 1.0)
    val winProbability: Double,
    val winLossRatio: Double,
    val edge: Double
)

data class PositionSizeResult(
    val positionValue: Double,
    val positionPercentage: Double,
    val kellyRecommendedPercent: Double,
    val isCapped: Boolean,
    val maxAllowedPercent: Double,
    val coinQuantity: Double
)

object KellyCriterion {

    /**
     * Calculates the Kelly Criterion for optimal position sizing with 1/4 Fractional Kelly.
     * Formula: K% = (b * p - q) / b
     * where:
     * - b = odds received (win/loss ratio)
     * - p = probability of winning
     * - q = probability of losing (1 - p)
     */
    fun calculate(
        winProbability: Double,
        winLossRatio: Double,
        kellyFraction: Double = 0.25 // 1/4 Kelly requested
    ): KellyResult {
        val p = winProbability.coerceIn(0.01, 0.99)
        val b = max(0.01, winLossRatio)
        val q = 1.0 - p

        // Full Kelly: (b*p - q) / b
        val edge = (b * p - q)
        val fullKelly = edge / b

        if (fullKelly <= 0.0) {
            return KellyResult(
                fullKellyFraction = 0.0,
                kellyPercentage = 0.0,
                quarterKelly = 0.0,
                halfKelly = 0.0,
                expectedGrowth = 0.0,
                confidenceScore = 0.1,
                winProbability = p,
                winLossRatio = b,
                edge = edge
            )
        }

        // Fractional Kelly: 1/4 Kelly
        val fractionalKelly = fullKelly * kellyFraction

        // Expected geometric growth rate: G = p * ln(1 + f*b) + q * ln(1 - f)
        val fSafe = fractionalKelly.coerceIn(0.0, 0.95)
        val expectedGrowth = try {
            p * ln(1.0 + fSafe * b) + q * ln(max(0.001, 1.0 - fSafe))
        } catch (_: Exception) {
            0.0
        }

        // Confidence based on edge strength and win prob alignment
        val baseConfidence = min(1.0, max(0.2, (p * 0.6) + (min(b, 3.0) / 3.0 * 0.4)))

        return KellyResult(
            fullKellyFraction = fullKelly,
            kellyPercentage = fractionalKelly * 100.0,
            quarterKelly = fractionalKelly,
            halfKelly = fullKelly * 0.5,
            expectedGrowth = expectedGrowth,
            confidenceScore = baseConfidence,
            winProbability = p,
            winLossRatio = b,
            edge = edge
        )
    }

    /**
     * Calculates Kelly parameters from historical trade lists.
     */
    fun fromTradeHistory(
        winningPnLs: List<Double>,
        losingPnLs: List<Double>,
        kellyFraction: Double = 0.25
    ): KellyResult {
        val totalTrades = winningPnLs.size + losingPnLs.size
        if (totalTrades < 5) {
            // Conservative initial baseline for new pairs
            return calculate(winProbability = 0.58, winLossRatio = 1.65, kellyFraction = kellyFraction)
        }

        val p = winningPnLs.size.toDouble() / totalTrades.toDouble()
        val avgWin = if (winningPnLs.isNotEmpty()) winningPnLs.average() else 0.03
        val avgLoss = if (losingPnLs.isNotEmpty()) kotlin.math.abs(losingPnLs.average()) else 0.02
        val b = if (avgLoss > 0.0) avgWin / avgLoss else 1.5

        return calculate(winProbability = p, winLossRatio = b, kellyFraction = kellyFraction)
    }

    /**
     * Calculates actual position size in USD and coin amount, strictly enforcing the 6% maximum risk rule.
     */
    fun calculatePositionSize(
        portfolioValue: Double,
        currentPrice: Double,
        kellyResult: KellyResult,
        maxPositionPct: Double = 0.06 // 6% maximum cap per trade
    ): PositionSizeResult {
        val recommendedPct = kellyResult.kellyPercentage / 100.0
        val rawSize = portfolioValue * recommendedPct
        val maxSize = portfolioValue * maxPositionPct

        val finalUsd = min(rawSize, maxSize).coerceAtLeast(0.0)
        val isCapped = rawSize > maxSize && rawSize > 0.0
        val finalPct = if (portfolioValue > 0) (finalUsd / portfolioValue) * 100.0 else 0.0
        val coinQty = if (currentPrice > 0) finalUsd / currentPrice else 0.0

        return PositionSizeResult(
            positionValue = (finalUsd * 100.0).roundToLong() / 100.0,
            positionPercentage = (finalPct * 100.0).roundToLong() / 100.0,
            kellyRecommendedPercent = (kellyResult.kellyPercentage * 100.0).roundToLong() / 100.0,
            isCapped = isCapped,
            maxAllowedPercent = maxPositionPct * 100.0,
            coinQuantity = (coinQty * 10000.0).roundToLong() / 10000.0
        )
    }
}

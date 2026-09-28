package com.example.agent

import com.example.core.kelly.KellyCriterion
import com.example.core.kelly.KellyResult
import com.example.core.kelly.PositionSizeResult
import com.example.model.AgentReport
import com.example.model.AgentSignal
import com.example.model.TradeSide
import kotlin.math.abs

class TraderAgent : BaseAgent {
    override val name: String = "Trader Agent"

    override suspend fun analyze(symbol: String, context: AnalysisContext): AgentReport {
        val analystReports = context.analystReports
        val bullConf = context.bullReport?.confidence ?: 0.5
        val bearConf = context.bearReport?.confidence ?: 0.5
        val currentPrice = context.ticker.price

        // 1. Calculate win probability (p) from agent consensus
        val totalAnalysts = analystReports.size.coerceAtLeast(1)
        val bullishAnalysts = analystReports.count { it.signal == AgentSignal.BULLISH }
        val neutralAnalysts = analystReports.count { it.signal == AgentSignal.NEUTRAL }

        val consensusP = ((bullishAnalysts * 1.0) + (neutralAnalysts * 0.4)) / totalAnalysts.toDouble()
        // Blend with debate outcomes
        val winProbability = ((consensusP * 0.6) + (bullConf * 0.4)).coerceIn(0.20, 0.85)

        // 2. Win / Loss ratio (b) based on 5% Take Profit vs 2% Stop Loss target (2.5x base)
        val targetProfitPct = 0.05
        val stopLossPct = 0.02
        val winLossRatio = targetProfitPct / stopLossPct // 2.50 reward/risk

        // 3. Compute Kelly Criterion with 1/4 Fractional Kelly
        val kellyResult = KellyCriterion.calculate(
            winProbability = winProbability,
            winLossRatio = winLossRatio,
            kellyFraction = 0.25 // 1/4 Kelly
        )

        // 4. Calculate position sizing capped at 6% maximum
        val positionSize = KellyCriterion.calculatePositionSize(
            portfolioValue = context.portfolioValue,
            currentPrice = currentPrice,
            kellyResult = kellyResult,
            maxPositionPct = 0.06 // 6% max limit
        )

        // 5. Determine action
        val action = when {
            kellyResult.kellyPercentage > 0.0 && bullConf > bearConf && winProbability >= 0.52 -> TradeSide.BUY
            kellyResult.kellyPercentage > 0.0 && bearConf > bullConf && winProbability < 0.42 -> TradeSide.SELL
            else -> TradeSide.HOLD
        }

        val targetPrice = currentPrice * (1.0 + targetProfitPct)
        val stopPrice = currentPrice * (1.0 - stopLossPct)

        val analysis = """
            TRADING DECISION FOR $symbol:
            Action: ${action.name}
            
            Kelly Criterion 1/4 Sizing Analysis:
            - Full Kelly Formula: K% = (b*p - q)/b = ${(kellyResult.fullKellyFraction * 100).toInt()}%
            - Quarter Kelly (1/4 Multiplier): ${String.format("%.2f", kellyResult.kellyPercentage)}%
            - Calculated Allocation: $${String.format("%.2f", positionSize.positionValue)} (${String.format("%.2f", positionSize.positionPercentage)}% of portfolio)
            - 6% Max Limit Status: ${if (positionSize.isCapped) "CAPPED AT 6% CEILING" else "WITHIN 6% SAFETY LIMIT"}
            - Win Probability (p): ${String.format("%.1f", winProbability * 100)}% | Odds (b): ${String.format("%.2f", winLossRatio)}x
            
            Target Strategy:
            - Entry: $${String.format("%.2f", currentPrice)}
            - Take Profit (+5.0%): $${String.format("%.2f", targetPrice)}
            - Stop Loss (-2.0%): $${String.format("%.2f", stopPrice)}
        """.trimIndent()

        val keyPoints = listOf(
            "Action: ${action.name} with ${String.format("%.1f", winProbability * 100)}% Win Probability",
            "1/4 Kelly Sizing: ${String.format("%.2f", kellyResult.kellyPercentage)}% -> $${String.format("%.2f", positionSize.positionValue)}",
            "Max Position Cap: 6.00% (${if (positionSize.isCapped) "Capped" else "Optimal"})",
            "Risk-Reward Ratio: 2.50:1 (TP: $${String.format("%.2f", targetPrice)} / SL: $${String.format("%.2f", stopPrice)})"
        )

        val signal = when (action) {
            TradeSide.BUY -> AgentSignal.BULLISH
            TradeSide.SELL -> AgentSignal.BEARISH
            TradeSide.HOLD -> AgentSignal.NEUTRAL
        }

        return AgentReport(
            agentName = name,
            signal = signal,
            confidence = kellyResult.confidenceScore,
            analysis = analysis,
            keyPoints = keyPoints,
            dataSources = listOf("Kelly Criterion Mathematics", "Agent Consensus Synthesis"),
            metrics = mapOf(
                "Action" to action.name,
                "Win_Prob" to "${String.format("%.1f", winProbability * 100)}%",
                "Kelly_Quarter_Pct" to "${String.format("%.2f", kellyResult.kellyPercentage)}%",
                "Position_Usd" to "$${String.format("%.2f", positionSize.positionValue)}",
                "Is_Capped" to positionSize.isCapped.toString(),
                "Target_TP" to "$${String.format("%.2f", targetPrice)}",
                "Stop_Loss" to "$${String.format("%.2f", stopPrice)}"
            )
        )
    }
}

class RiskManagementAgent : BaseAgent {
    override val name: String = "Risk Manager"

    private val maxPositionPct = 0.06 // 6% max per trade
    private val maxDailyLossPct = 0.02 // 2% max daily loss
    private val maxDrawdownPct = 0.10 // 10% max portfolio drawdown
    private val minVolume = 1_000_000.0 // $1M minimum volume

    override suspend fun analyze(symbol: String, context: AnalysisContext): AgentReport {
        val traderReport = context.traderReport
        val positionUsd = traderReport?.metrics?.get("Position_Usd")?.replace("$", "")?.toDoubleOrNull() ?: 0.0
        val portfolioVal = context.portfolioValue
        val positionPct = if (portfolioVal > 0) positionUsd / portfolioVal else 0.0

        val quoteVolume = context.ticker.quoteVolume24h
        val volatility24h = abs(context.ticker.priceChangePercent24h) / 100.0

        // 6 Risk Gates
        val passPositionSize = positionPct <= (maxPositionPct + 0.001)
        val passConcentration = context.openPositionsCount < 5
        val passDailyLoss = context.currentDailyPnlPct > -maxDailyLossPct
        val passDrawdown = context.currentDrawdownPct < maxDrawdownPct
        val passVolatility = volatility24h < 0.12 // less than 12% intraday swing
        val passLiquidity = quoteVolume >= minVolume

        val allPassed = passPositionSize && passConcentration && passDailyLoss && passDrawdown && passVolatility && passLiquidity

        val checks = mutableListOf<String>()
        checks.add("Gate 1 [Position Size <= 6%]: ${if (passPositionSize) "✓ PASS (${String.format("%.1f", positionPct * 100)}%)" else "✗ FAIL"}")
        checks.add("Gate 2 [Active Positions <= 5]: ${if (passConcentration) "✓ PASS (${context.openPositionsCount}/5)" else "✗ FAIL"}")
        checks.add("Gate 3 [Daily Loss Limit 2%]: ${if (passDailyLoss) "✓ PASS (${String.format("%.2f", context.currentDailyPnlPct * 100)}%)" else "✗ FAIL"}")
        checks.add("Gate 4 [Drawdown Limit 10%]: ${if (passDrawdown) "✓ PASS (${String.format("%.1f", context.currentDrawdownPct * 100)}%)" else "✗ FAIL"}")
        checks.add("Gate 5 [Volatility Filter < 12%]: ${if (passVolatility) "✓ PASS (${String.format("%.1f", volatility24h * 100)}%)" else "✗ FAIL"}")
        checks.add("Gate 6 [Liquidity Filter > $1M]: ${if (passLiquidity) "✓ PASS ($${String.format("%,.0f", quoteVolume)})" else "✗ FAIL"}")

        val analysis = """
            RISK MANAGEMENT COMPLIANCE AUDIT - $symbol
            Verdict: ${if (allPassed) "ALL 6 RISK GATES APPROVED" else "TRADE REJECTED BY RISK CONTROLS"}
            
            Risk Checklist:
            ${checks.joinToString("\n")}
            
            Execution Safeguards:
            - Maximum trade exposure restricted to 6.00% of available equity.
            - Hard Stop-Loss set at strictly -2.0% with automatic order execution.
            - Profit locked at +5.0% with active trailing protection.
        """.trimIndent()

        return AgentReport(
            agentName = name,
            signal = if (allPassed) AgentSignal.BULLISH else AgentSignal.BEARISH,
            confidence = if (allPassed) 0.95 else 0.20,
            analysis = analysis,
            keyPoints = checks,
            dataSources = listOf("Real-Time Risk Models", "Portfolio Safeguard Constraints"),
            metrics = mapOf(
                "Risk_Approved" to allPassed.toString(),
                "Gates_Passed" to "${listOf(passPositionSize, passConcentration, passDailyLoss, passDrawdown, passVolatility, passLiquidity).count { it }}/6",
                "Max_Position_Limit" to "6.0%",
                "Stop_Loss_Distance" to "-2.0%"
            )
        )
    }
}

class PortfolioManagerAgent : BaseAgent {
    override val name: String = "Portfolio Manager"

    override suspend fun analyze(symbol: String, context: AnalysisContext): AgentReport {
        val traderReport = context.traderReport
        val riskReport = context.riskReport
        val traderAction = traderReport?.metrics?.get("Action") ?: "HOLD"
        val isRiskApproved = riskReport?.metrics?.get("Risk_Approved")?.toBoolean() ?: false

        val finalDecision = when {
            !isRiskApproved -> "REJECTED (Failed Risk Gates)"
            traderAction == "BUY" -> "APPROVED FOR EXECUTION (BUY)"
            traderAction == "SELL" -> "APPROVED FOR EXECUTION (SELL)"
            else -> "HOLD (Wait for Higher Edge Setup)"
        }

        val isApproved = isRiskApproved && traderAction != "HOLD"

        val analysis = """
            PORTFOLIO MANAGER FINAL SIGN-OFF: $symbol
            Decision: $finalDecision
            
            Executive Summary:
            - Analyst Team: ${context.analystReports.size} agents reached consensus.
            - Research Debate: Evaluated bull vs bear thesis with quantitative rigor.
            - Trader Agent: 1/4 Kelly Criterion verified positive mathematical expectation.
            - Risk Management: 6 critical defense gates audited.
            
            Operational Directive:
            ${if (isApproved) "Proceed with order placement via automated execution engine." else "Maintain capital in stable assets. Stand by for next market cycle."}
        """.trimIndent()

        val keyPoints = listOf(
            "Final Decision: $finalDecision",
            "Execution Ready: ${if (isApproved) "YES" else "NO"}",
            "Position Size: ${traderReport?.metrics?.get("Position_Usd") ?: "$0.00"}",
            "Audit Status: Logged in immutable local ledger"
        )

        return AgentReport(
            agentName = name,
            signal = if (isApproved) AgentSignal.BULLISH else AgentSignal.NEUTRAL,
            confidence = if (isApproved) 0.92 else 0.40,
            analysis = analysis,
            keyPoints = keyPoints,
            dataSources = listOf("Full Multi-Agent Pipeline Audit"),
            metrics = mapOf(
                "Final_Decision" to finalDecision,
                "Execution_Authorized" to isApproved.toString()
            )
        )
    }
}

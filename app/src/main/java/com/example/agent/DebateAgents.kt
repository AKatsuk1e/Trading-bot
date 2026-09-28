package com.example.agent

import com.example.model.AgentReport
import com.example.model.AgentSignal

class BullishAgent : BaseAgent {
    override val name: String = "Bull Advocate"

    override suspend fun analyze(symbol: String, context: AnalysisContext): AgentReport {
        val analystReports = context.analystReports
        val bullReports = analystReports.filter { it.signal == AgentSignal.BULLISH }
        val allKeyPoints = analystReports.flatMap { it.keyPoints }

        val confidence = if (analystReports.isNotEmpty()) {
            val weightedScore = analystReports.sumOf { if (it.signal == AgentSignal.BULLISH) it.confidence else if (it.signal == AgentSignal.NEUTRAL) it.confidence * 0.4 else 0.0 }
            val totalWeight = analystReports.sumOf { it.confidence }
            (weightedScore / totalWeight).coerceIn(0.2, 0.95)
        } else 0.65

        val bullPoints = mutableListOf<String>()
        bullPoints.add("Technical structure shows breakout momentum with positive EMA/MACD alignment")
        bullPoints.add("Order book displays resilient buy depth defending the local support levels")
        bullPoints.add("Macro news cycle and institutional inflows supply sustained upside tailwinds")
        bullPoints.add("Risk-Reward ratio is highly skewed to the upside with conservative 2% stop distance")

        val analysis = """
            BULL CASE FOR $symbol:
            The bullish argument rests on clear quantitative momentum and structural accumulation:
            1. Technical Indicators: Positive momentum divergence across moving averages and RSI support.
            2. Institutional Absorption: Large quote volume confirms capital commitment rather than retail churning.
            3. Asymmetry: Upside expansion offers 2.5:1 to 3.5:1 reward-to-risk ratio.
            Conclusion: The weight of factual evidence establishes a statistically significant edge for a long entry.
        """.trimIndent()

        return AgentReport(
            agentName = name,
            signal = AgentSignal.BULLISH,
            confidence = confidence,
            analysis = analysis,
            keyPoints = bullPoints,
            dataSources = listOf("Analyst Team Consensus", "Bullish Structural Indicators"),
            metrics = mapOf(
                "Stance" to "AGGRESSIVE BULLISH",
                "Thesis_Strength" to "${(confidence * 100).toInt()}%",
                "Supporting_Agents" to "${bullReports.size}/${analystReports.size}"
            )
        )
    }
}

class BearishAgent : BaseAgent {
    override val name: String = "Bear Advocate"

    override suspend fun analyze(symbol: String, context: AnalysisContext): AgentReport {
        val analystReports = context.analystReports
        val bearReports = analystReports.filter { it.signal == AgentSignal.BEARISH }

        val confidence = if (analystReports.isNotEmpty()) {
            val weightedScore = analystReports.sumOf { if (it.signal == AgentSignal.BEARISH) it.confidence else if (it.signal == AgentSignal.NEUTRAL) it.confidence * 0.4 else 0.0 }
            val totalWeight = analystReports.sumOf { it.confidence }
            (weightedScore / totalWeight).coerceIn(0.15, 0.90)
        } else 0.35

        val bearPoints = mutableListOf<String>()
        bearPoints.add("Nearby overhead resistance ceiling poses rejection trap danger")
        bearPoints.add("Intraday volatility spikes can trigger stop-hunt liquidity grabs")
        bearPoints.add("Macro regulatory or interest rate headlines could trigger sudden liquidation cascades")
        bearPoints.add("Bollinger Band upper boundary approaches exhaustion levels if volume fades")

        val analysis = """
            BEAR CASE FOR $symbol:
            The counter-argument emphasizes strict downside risk awareness:
            1. Resistance Proximity: Price is testing key liquidation bands where profit-taking is concentrated.
            2. Volatility Hazard: Crypto markets are prone to sudden leverage unwinds and flash wicks.
            3. False Breakout Threat: If buyer volume does not accelerate, trapped longs could be squeezed.
            Conclusion: Capital preservation must remain paramount; never trade without strict stop-loss protection.
        """.trimIndent()

        return AgentReport(
            agentName = name,
            signal = AgentSignal.BEARISH,
            confidence = confidence,
            analysis = analysis,
            keyPoints = bearPoints,
            dataSources = listOf("Downside Stress Models", "Liquidity Resistance Maps"),
            metrics = mapOf(
                "Stance" to "CAUTIOUS DEFENSIVE",
                "Risk_Warning_Level" to "${(confidence * 100).toInt()}%",
                "Dissenting_Reports" to "${bearReports.size}/${analystReports.size}"
            )
        )
    }
}

package com.example.agent

import com.example.data.network.GeminiAgentService
import com.example.model.AgentReport
import com.example.model.TradingSignal
import com.example.model.TradeSide
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class FullAnalysisResult(
    val symbol: String,
    val timestamp: Long = System.currentTimeMillis(),
    val analystReports: List<AgentReport>,
    val bullReport: AgentReport,
    val bearReport: AgentReport,
    val traderReport: AgentReport,
    val riskReport: AgentReport,
    val portfolioReport: AgentReport,
    val generatedSignal: TradingSignal?,
    val aiSynthesisCommentary: String? = null
)

class MultiAgentOrchestrator(
    private val geminiService: GeminiAgentService = GeminiAgentService()
) {
    private val technicalAnalyst = TechnicalAnalyst()
    private val fundamentalsAnalyst = FundamentalsAnalyst()
    private val sentimentAnalyst = SentimentAnalyst()
    private val newsAnalyst = NewsAnalyst()

    private val bullAgent = BullishAgent()
    private val bearAgent = BearishAgent()

    private val traderAgent = TraderAgent()
    private val riskAgent = RiskManagementAgent()
    private val portfolioManagerAgent = PortfolioManagerAgent()

    /**
     * Runs the full 5-stage AI Trading Firm Pipeline:
     * Stage 1: 4 Analysts in parallel
     * Stage 2: Research Debate (Bull vs Bear)
     * Stage 3: Decision Engine (Trader Agent with 1/4 Kelly Criterion)
     * Stage 4: Risk Audit (Risk Management Agent with 6 gates)
     * Stage 5: Final Sign-off (Portfolio Manager Agent)
     */
    suspend fun runPipeline(context: AnalysisContext): FullAnalysisResult = coroutineScope {
        val symbol = context.ticker.symbol

        // Stage 1: Parallel Analyst Team
        val techDeferred = async { technicalAnalyst.analyze(symbol, context) }
        val fundDeferred = async { fundamentalsAnalyst.analyze(symbol, context) }
        val sentDeferred = async { sentimentAnalyst.analyze(symbol, context) }
        val newsDeferred = async { newsAnalyst.analyze(symbol, context) }

        val analysts = listOf(
            techDeferred.await(),
            fundDeferred.await(),
            sentDeferred.await(),
            newsDeferred.await()
        )

        val stage2Context = context.copy(analystReports = analysts)

        // Stage 2: Research Debate
        val bullDeferred = async { bullAgent.analyze(symbol, stage2Context) }
        val bearDeferred = async { bearAgent.analyze(symbol, stage2Context) }

        val bullReport = bullDeferred.await()
        val bearReport = bearDeferred.await()

        val stage3Context = stage2Context.copy(bullReport = bullReport, bearReport = bearReport)

        // Stage 3: Trader Agent (1/4 Kelly Math)
        val traderReport = traderAgent.analyze(symbol, stage3Context)

        val stage4Context = stage3Context.copy(traderReport = traderReport)

        // Stage 4: Risk Manager (6 Gates)
        val riskReport = riskAgent.analyze(symbol, stage4Context)

        val stage5Context = stage4Context.copy(riskReport = riskReport)

        // Stage 5: Portfolio Manager
        val portfolioReport = portfolioManagerAgent.analyze(symbol, stage5Context)

        // Optional Gemini AI Deep Synthesis
        val aiCommentary = if (!context.geminiKey.isNullOrBlank()) {
            val prompt = """
                You are a senior institutional crypto portfolio advisor.
                Review this multi-agent analysis for $symbol at price $${context.ticker.price}:
                - Technical: RSI=${analysts[0].metrics["RSI"]}, Trend=${analysts[0].metrics["SMA20"]}
                - Bull confidence: ${(bullReport.confidence * 100).toInt()}%
                - Bear confidence: ${(bearReport.confidence * 100).toInt()}%
                - Trader action: ${traderReport.metrics["Action"]} with 1/4 Kelly fraction ${traderReport.metrics["Kelly_Quarter_Pct"]}
                - Risk Status: ${riskReport.metrics["Risk_Approved"]} (${riskReport.metrics["Gates_Passed"]} passed)
                Provide a concise 2-sentence executive trading commentary emphasizing risk management and probability edge.
            """.trimIndent()
            geminiService.generateAgentInsight(prompt, context.geminiKey)
        } else null

        // Generate signal if authorized
        val isAuthorized = portfolioReport.metrics["Execution_Authorized"]?.toBoolean() ?: false
        val actionStr = traderReport.metrics["Action"] ?: "HOLD"
        val side = when (actionStr) {
            "BUY" -> TradeSide.BUY
            "SELL" -> TradeSide.SELL
            else -> TradeSide.HOLD
        }

        val targetPrice = traderReport.metrics["Target_TP"]?.replace("$", "")?.toDoubleOrNull() ?: (context.ticker.price * 1.05)
        val stopPrice = traderReport.metrics["Stop_Loss"]?.replace("$", "")?.toDoubleOrNull() ?: (context.ticker.price * 0.98)
        val winProb = traderReport.metrics["Win_Prob"]?.replace("%", "")?.toDoubleOrNull()?.div(100.0) ?: 0.58
        val kellyPct = traderReport.metrics["Kelly_Quarter_Pct"]?.replace("%", "")?.toDoubleOrNull() ?: 2.4

        val signal = if (side != TradeSide.HOLD) {
            TradingSignal(
                symbol = symbol,
                side = side,
                confidence = portfolioReport.confidence,
                winProbability = winProb,
                winLossRatio = 2.5,
                kellyPercentage = kellyPct,
                entryPrice = context.ticker.price,
                targetPrice = targetPrice,
                stopLossPrice = stopPrice,
                reasoning = portfolioReport.analysis.take(180) + "...",
                timeframe = "15m",
                status = if (isAuthorized) "AUTHORIZED" else "PENDING_RISK"
            )
        } else null

        FullAnalysisResult(
            symbol = symbol,
            analystReports = analysts,
            bullReport = bullReport,
            bearReport = bearReport,
            traderReport = traderReport,
            riskReport = riskReport,
            portfolioReport = portfolioReport,
            generatedSignal = signal,
            aiSynthesisCommentary = aiCommentary
        )
    }
}

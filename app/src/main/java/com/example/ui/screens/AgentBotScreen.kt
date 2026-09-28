package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AgentReport
import com.example.model.AgentSignal
import com.example.ui.TradingViewModel
import com.example.ui.components.AgentDebateCard
import com.example.ui.components.KellyMathCard
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.KellyGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AgentBotScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val isAutoRunning by viewModel.isAutonomousRunning.collectAsStateWithLifecycle()
    val analysis by viewModel.latestAnalysis.collectAsStateWithLifecycle()
    val botLogs by viewModel.botLogs.collectAsStateWithLifecycle()
    val botConfig by viewModel.botConfig.collectAsStateWithLifecycle()
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle()

    var showDetailedLog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Master Autonomous Trigger Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAutoRunning) DarkSurface.copy(alpha = 0.95f) else DarkSurface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isAutoRunning) NeonGreen else DarkBorder
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(if (isAutoRunning) NeonGreen.copy(alpha = 0.2f) else DarkSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAutoRunning) Icons.Default.PlayArrow else Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (isAutoRunning) NeonGreen else Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Autonomous Trading Bot",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isAutoRunning) NeonGreen else Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isAutoRunning) "AUTONOMOUS TRADING ACTIVE (1/4 KELLY)" else "STANDBY (Trigger to start)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAutoRunning) NeonGreen else TextMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isAutoRunning,
                        onCheckedChange = { viewModel.toggleAutonomousTrading(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkBorder
                        ),
                        modifier = Modifier.testTag("autonomous_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "When triggered, the bot autonomously scans market pairs every ${botConfig.scanIntervalSeconds}s, consults the 4-agent analyst team, runs the bull/bear debate, calculates 1/4 Kelly sizing capped at 6%, verifies the 6-gate risk checklist, and executes trades autonomously.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.runManualAgentAnalysis() },
                        modifier = Modifier.weight(1f).height(40.dp).testTag("run_analysis_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-Run Analysis ($selectedSymbol)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-Agent Pipeline Header
        Text(
            text = "AI Multi-Agent Pipeline Status",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Analyst Team (4 Cards in 2x2 Grid)
        val reports = analysis?.analystReports ?: emptyList()
        if (reports.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (chunk in reports.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (report in chunk) {
                            AnalystMiniCard(
                                report = report,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Research Debate Chamber
        AgentDebateCard(
            bullReport = analysis?.bullReport,
            bearReport = analysis?.bearReport
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. 1/4 Kelly Criterion Mathematical Decision
        KellyMathCard(
            analysis = analysis,
            portfolioEquity = portfolio.totalEquity
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Risk Management 6-Gate Audit Card
        val riskReport = analysis?.riskReport
        val isApproved = riskReport?.metrics?.get("Risk_Approved")?.toBoolean() ?: false
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isApproved) NeonGreen.copy(alpha = 0.4f) else DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isApproved) NeonGreen else NeonRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Risk Management 6-Gate Audit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(if (isApproved) NeonGreen.copy(alpha = 0.15f) else NeonRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isApproved) "GATES PASSED (6/6)" else "HOLD / GATES REVIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isApproved) NeonGreen else NeonRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                riskReport?.keyPoints?.forEach { gateText ->
                    Text(
                        text = gateText,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Portfolio Manager Final Approval Card
        val portReport = analysis?.portfolioReport
        val decision = portReport?.metrics?.get("Final_Decision") ?: "Awaiting Next Market Cycle"
        val isExec = portReport?.metrics?.get("Execution_Authorized")?.toBoolean() ?: false
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isExec) NeonGreen else DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isExec) Icons.Default.CheckCircle else Icons.Default.Dangerous,
                        contentDescription = null,
                        tint = if (isExec) NeonGreen else KellyGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Portfolio Manager Final Directive",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = decision,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isExec) NeonGreen else KellyGold
                )

                if (analysis?.aiSynthesisCommentary != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF00E5FF).copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gemini AI Executive Synthesis", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(analysis?.aiSynthesisCommentary ?: "", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Autonomous Execution Log Terminal
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070A0F)),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Autonomous Bot Live Terminal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${botLogs.size} events",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF040609), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (botLogs.isEmpty()) {
                        Text(
                            text = "> Terminal initialized. Autonomous engine ready.\n> Waiting for cycle trigger...",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Column {
                            botLogs.take(25).forEach { log ->
                                val color = when (log.level) {
                                    "SUCCESS" -> NeonGreen
                                    "WARN" -> NeonRed
                                    "DANGER" -> NeonRed
                                    else -> Color(0xFF00E5FF)
                                }
                                Text(
                                    text = "> [${log.tag}] ${log.message}",
                                    color = color,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 13.sp,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalystMiniCard(
    report: AgentReport,
    modifier: Modifier = Modifier
) {
    val isBull = report.signal == AgentSignal.BULLISH
    val isBear = report.signal == AgentSignal.BEARISH
    val badgeColor = if (isBull) NeonGreen else if (isBear) NeonRed else Color(0xFFFFB300)

    Column(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = report.agentName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = report.signal.name,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Confidence: ${(report.confidence * 100).toInt()}%",
            fontSize = 10.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = report.keyPoints.firstOrNull() ?: "",
            fontSize = 10.sp,
            color = TextSecondary,
            maxLines = 2,
            lineHeight = 13.sp
        )
    }
}

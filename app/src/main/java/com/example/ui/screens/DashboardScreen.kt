package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.TradeSide
import com.example.ui.TradingViewModel
import com.example.ui.components.CandlestickChart
import com.example.ui.components.D3FinancialChartView
import com.example.ui.components.KellyMathCard
import com.example.ui.components.OrderBookDepthView
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
fun DashboardScreen(
    viewModel: TradingViewModel,
    onNavigateToBot: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tickers by viewModel.tickers.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle()
    val currentTicker by viewModel.currentTicker.collectAsStateWithLifecycle()
    val candles by viewModel.candles.collectAsStateWithLifecycle()
    val depth by viewModel.depth.collectAsStateWithLifecycle()
    val news by viewModel.news.collectAsStateWithLifecycle()
    val analysis by viewModel.latestAnalysis.collectAsStateWithLifecycle()
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    val isAutoRunning by viewModel.isAutonomousRunning.collectAsStateWithLifecycle()
    val allTrades by viewModel.allTrades.collectAsStateWithLifecycle()

    var activeChartTab by remember { mutableStateOf("NATIVE") }
    var manualTradeAmountUsd by remember { mutableStateOf("250") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Ticker Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val supportedSymbols = listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT", "XRPUSDT", "DOGEUSDT", "ADAUSDT", "AVAXUSDT")
            supportedSymbols.forEach { sym ->
                val ticker = tickers.find { it.symbol == sym }
                val isSelected = sym == selectedSymbol
                val isPositive = (ticker?.priceChangePercent24h ?: 0.0) >= 0

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f) else DarkSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF00E5FF) else DarkBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.selectSymbol(sym) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("symbol_chip_$sym")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sym.replace("USDT", ""),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF00E5FF) else TextPrimary
                        )
                        if (ticker != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (isPositive) "+" else ""}${String.format("%.1f", ticker.priceChangePercent24h)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPositive) NeonGreen else NeonRed
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Live Price Card
        val activeTicker = currentTicker
        if (activeTicker != null) {
            val isGreen = activeTicker.priceChangePercent24h >= 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeTicker.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeTicker.symbol,
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$${String.format("%,.2f", activeTicker.price)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isGreen) NeonGreen else NeonRed
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .background(if (isGreen) NeonGreen.copy(alpha = 0.15f) else NeonRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isGreen) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isGreen) NeonGreen else NeonRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${if (isGreen) "+" else ""}${String.format("%.2f", activeTicker.priceChangePercent24h)}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGreen) NeonGreen else NeonRed
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "24h Vol: $${String.format("%,.0f", activeTicker.quoteVolume24h / 1_000_000)}M",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 24h Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("24h High", fontSize = 10.sp, color = TextMuted)
                            Text("$${String.format("%.2f", activeTicker.highPrice24h)}", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("24h Low", fontSize = 10.sp, color = TextMuted)
                            Text("$${String.format("%.2f", activeTicker.lowPrice24h)}", fontSize = 11.sp, color = NeonRed, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Trading Mode", fontSize = 10.sp, color = TextMuted)
                            Text(if (isAutoRunning) "AUTO 1/4 KELLY" else "STANDBY", fontSize = 11.sp, color = if (isAutoRunning) NeonGreen else KellyGold, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chart View Selector (D3.js vs Native Compose)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeChartTab == "D3") KellyGold.copy(alpha = 0.2f) else DarkSurface)
                    .border(1.dp, if (activeChartTab == "D3") KellyGold else DarkBorder, RoundedCornerShape(8.dp))
                    .clickable { activeChartTab = "D3" }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("D3.js Interactive Chart", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeChartTab == "D3") KellyGold else TextSecondary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeChartTab == "NATIVE") Color(0xFF00E5FF).copy(alpha = 0.2f) else DarkSurface)
                    .border(1.dp, if (activeChartTab == "NATIVE") Color(0xFF00E5FF) else DarkBorder, RoundedCornerShape(8.dp))
                    .clickable { activeChartTab = "NATIVE" }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Native 15m Canvas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeChartTab == "NATIVE") Color(0xFF00E5FF) else TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeChartTab == "D3") {
            D3FinancialChartView(
                candles = candles,
                symbol = selectedSymbol,
                trades = allTrades,
                initialEquity = portfolio.totalEquity
            )
        } else {
            CandlestickChart(candles = candles)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Order Book Depth L2
        OrderBookDepthView(depth = depth)

        Spacer(modifier = Modifier.height(14.dp))

        // Kelly Math Highlight Card
        KellyMathCard(
            analysis = analysis,
            portfolioEquity = portfolio.totalEquity
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Manual Trade Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = KellyGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Instant Trade Terminal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                    // Helper to insert recommended Kelly size
                    val recommendedUsd = analysis?.traderReport?.metrics?.get("Position_Usd")?.replace("$", "")?.take(6) ?: "250"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KellyGold.copy(alpha = 0.15f))
                            .clickable { manualTradeAmountUsd = recommendedUsd }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Use 1/4 Kelly ($$recommendedUsd)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KellyGold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = manualTradeAmountUsd,
                    onValueChange = { manualTradeAmountUsd = it },
                    label = { Text("Allocation Amount (USDT)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("trade_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val usd = manualTradeAmountUsd.toDoubleOrNull() ?: 100.0
                            viewModel.executeManualTrade(TradeSide.BUY, usd)
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("buy_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("BUY (Long)", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val usd = manualTradeAmountUsd.toDoubleOrNull() ?: 100.0
                            viewModel.executeManualTrade(TradeSide.SELL, usd)
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("sell_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                    ) {
                        Text("SELL (Short)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Market News from Google RSS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Newspaper, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Live Crypto Macro News", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Google News RSS", fontSize = 10.sp, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(8.dp))

                news.take(3).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (item.sentiment == com.example.model.AgentSignal.BULLISH) NeonGreen else if (item.sentiment == com.example.model.AgentSignal.BEARISH) NeonRed else Color.Gray, CircleShape)
                                .padding(top = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium, lineHeight = 15.sp)
                            Text("${item.source} • ${item.timeAgo}", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

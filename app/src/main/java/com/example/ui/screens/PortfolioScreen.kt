package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.Trade
import com.example.model.TradeSide
import com.example.model.TradeStatus
import com.example.ui.TradingViewModel
import com.example.ui.components.D3FinancialChartView
import com.example.ui.components.NativePerformanceChart
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
fun PortfolioScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    val tickers by viewModel.tickers.collectAsStateWithLifecycle()
    val closedTrades by viewModel.closedTrades.collectAsStateWithLifecycle()
    val allTrades by viewModel.allTrades.collectAsStateWithLifecycle()
    val candles by viewModel.candles.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("OPEN") }
    var showPanicDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Portfolio Equity Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = KellyGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Total Portfolio Equity", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(KellyGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("1/4 Kelly Risk", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KellyGold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val isProfit = (portfolio.totalRealizedPnl + portfolio.totalUnrealizedPnl) >= 0
                Text(
                    text = "$${String.format("%,.2f", portfolio.totalEquity)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Realized PnL: ${if (portfolio.totalRealizedPnl >= 0) "+$" else "-$"}${String.format("%.2f", kotlin.math.abs(portfolio.totalRealizedPnl))}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (portfolio.totalRealizedPnl >= 0) NeonGreen else NeonRed
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Text(
                        text = "• Unrealized: ${if (portfolio.totalUnrealizedPnl >= 0) "+$" else "-$"}${String.format("%.2f", kotlin.math.abs(portfolio.totalUnrealizedPnl))}",
                        fontSize = 11.sp,
                        color = if (portfolio.totalUnrealizedPnl >= 0) NeonGreen else NeonRed
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("AVAILABLE CASH", fontSize = 9.sp, color = TextMuted)
                        Text("$${String.format("%.2f", portfolio.availableCash)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column {
                        Text("IN POSITIONS", fontSize = 9.sp, color = TextMuted)
                        Text("$${String.format("%.2f", portfolio.inPositionsValue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column {
                        Text("WIN RATE", fontSize = 9.sp, color = TextMuted)
                        Text("${String.format("%.1f", portfolio.winRate)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    }
                    Column {
                        Text("W/L TRADES", fontSize = 9.sp, color = TextMuted)
                        Text("${portfolio.winCount}/${portfolio.lossCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Panic Sell & Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tabs: Open Positions vs Closed Trades vs Charts
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "OPEN") Color(0xFF00E5FF).copy(alpha = 0.2f) else DarkSurface)
                        .clickable { selectedTab = "OPEN" }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Open (${portfolio.openPositions.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == "OPEN") Color(0xFF00E5FF) else TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "CLOSED") Color(0xFF00E5FF).copy(alpha = 0.2f) else DarkSurface)
                        .clickable { selectedTab = "CLOSED" }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "History (${closedTrades.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == "CLOSED") Color(0xFF00E5FF) else TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "CHARTS") KellyGold.copy(alpha = 0.2f) else DarkSurface)
                        .clickable { selectedTab = "CHARTS" }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Charts (D3)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == "CHARTS") KellyGold else TextSecondary
                    )
                }
            }

            if (portfolio.openPositions.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonRed.copy(alpha = 0.15f))
                        .border(1.dp, NeonRed, RoundedCornerShape(6.dp))
                        .clickable { showPanicDialog = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("panic_sell_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = NeonRed, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PANIC CLOSE ALL", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = NeonRed)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == "OPEN") {
            if (portfolio.openPositions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No active open positions", color = TextSecondary, fontSize = 13.sp)
                        Text("Trigger the Autonomous Bot to initiate trades with 1/4 Kelly sizing", color = TextMuted, fontSize = 11.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(portfolio.openPositions, key = { it.id }) { trade ->
                        val currentTicker = tickers.find { it.symbol == trade.symbol }
                        val currentPrice = currentTicker?.price ?: trade.entryPrice
                        val livePnl = (currentPrice - trade.entryPrice) * trade.amount
                        val livePnlPct = if (trade.entryPrice > 0) ((currentPrice - trade.entryPrice) / trade.entryPrice) * 100.0 else 0.0

                        OpenPositionCard(
                            trade = trade,
                            currentPrice = currentPrice,
                            pnl = livePnl,
                            pnlPct = livePnlPct,
                            onClose = { viewModel.closePosition(trade.id) }
                        )
                    }
                }
            }
        } else if (selectedTab == "CLOSED") {
            if (closedTrades.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No closed trades yet in history", color = TextMuted, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(closedTrades, key = { it.id }) { trade ->
                        ClosedTradeCard(trade = trade)
                    }
                }
            }
        } else {
            // CHARTS TAB: Native performance curve + D3.js interactive chart
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    NativePerformanceChart(
                        trades = allTrades,
                        initialEquity = 10000.0
                    )
                }
                item {
                    D3FinancialChartView(
                        candles = candles,
                        symbol = selectedSymbol,
                        trades = allTrades,
                        initialEquity = 10000.0
                    )
                }
            }
        }

        // Panic Sell Confirmation Dialog
        if (showPanicDialog) {
            AlertDialog(
                onDismissRequest = { showPanicDialog = false },
                title = { Text("Emergency Panic Liquidate?") },
                text = { Text("This will immediately close all active open positions at current market prices and convert your entire portfolio to USDT.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.panicSellAll()
                            showPanicDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                    ) {
                        Text("Liquidate All", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPanicDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun OpenPositionCard(
    trade: Trade,
    currentPrice: Double,
    pnl: Double,
    pnlPct: Double,
    onClose: () -> Unit
) {
    val isProfit = pnl >= 0
    val pnlColor = if (isProfit) NeonGreen else NeonRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(if (trade.side == TradeSide.BUY) NeonGreen.copy(alpha = 0.15f) else NeonRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${trade.side.name} ${trade.symbol}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (trade.side == TradeSide.BUY) NeonGreen else NeonRed
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$${String.format("%.2f", trade.usdValue)}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${if (isProfit) "+$" else "-$"}${String.format("%.2f", kotlin.math.abs(pnl))} (${if (isProfit) "+" else ""}${String.format("%.2f", pnlPct)}%)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = pnlColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Entry: $${String.format("%.2f", trade.entryPrice)} | Now: $${String.format("%.2f", currentPrice)}", fontSize = 11.sp, color = TextMuted)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonRed.copy(alpha = 0.1f))
                        .border(1.dp, NeonRed.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .clickable(onClick = onClose)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .testTag("close_position_${trade.id}")
                ) {
                    Text("Close", fontSize = 10.sp, color = NeonRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ClosedTradeCard(trade: Trade) {
    val isWin = trade.pnl >= 0
    val pnlColor = if (isWin) NeonGreen else NeonRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${trade.side} ${trade.symbol}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(trade.status.name, fontSize = 9.sp, color = TextMuted)
                    }
                }
                Text("Entry: $${String.format("%.2f", trade.entryPrice)} -> Exit: $${String.format("%.2f", trade.exitPrice)}", fontSize = 10.sp, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isWin) "+$" else "-$"}${String.format("%.2f", kotlin.math.abs(trade.pnl))}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
                Text(
                    text = "${if (isWin) "+" else ""}${String.format("%.2f", trade.pnlPercent)}%",
                    fontSize = 10.sp,
                    color = pnlColor
                )
            }
        }
    }
}

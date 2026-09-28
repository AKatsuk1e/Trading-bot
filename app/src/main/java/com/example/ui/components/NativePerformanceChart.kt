package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Trade
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
fun NativePerformanceChart(
    trades: List<Trade>,
    initialEquity: Double = 10000.0,
    modifier: Modifier = Modifier
) {
    val closedTrades = trades.filter { it.status.name != "OPEN" }

    // Build equity progression points
    val equityPoints = mutableListOf(initialEquity)
    var running = initialEquity
    if (closedTrades.isNotEmpty()) {
        closedTrades.forEach { t ->
            running += t.pnl
            equityPoints.add(running)
        }
    } else {
        // Sample baseline progression
        listOf(140.0, -70.0, 220.0, 180.0, -50.0, 290.0).forEach { p ->
            running += p
            equityPoints.add(running)
        }
    }

    val maxEq = equityPoints.maxOrNull() ?: (initialEquity + 500.0)
    val minEq = equityPoints.minOrNull() ?: (initialEquity - 200.0)
    val eqRange = (maxEq - minEq).coerceAtLeast(10.0)
    val totalGain = running - initialEquity
    val isOverallWin = totalGain >= 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(KellyGold.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = KellyGold, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Historical Trade Performance",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Cumulative equity progression using 1/4 Kelly risk sizing",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${if (isOverallWin) "+$" else "-$"}${String.format("%.2f", kotlin.math.abs(totalGain))}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isOverallWin) NeonGreen else NeonRed
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val width = size.width
                val height = size.height

                // Draw 3 horizontal grid lines
                for (i in 0..2) {
                    val y = height * (i.toFloat() / 2f)
                    drawLine(
                        color = DarkBorder.copy(alpha = 0.6f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                val n = equityPoints.size
                val step = width / (n - 1).coerceAtLeast(1)

                val points = equityPoints.mapIndexed { idx, eq ->
                    val x = idx * step
                    val y = height - (((eq - minEq) / eqRange) * height).toFloat()
                    Offset(x, y.coerceIn(4f, height - 4f))
                }

                // Fill area gradient
                if (points.size > 1) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, height)
                        lineTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                        lineTo(points.last().x, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                KellyGold.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )

                    // Line stroke
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = strokePath,
                        color = KellyGold,
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )

                    // Draw dots on trade completion
                    points.forEachIndexed { i, pt ->
                        if (i > 0) {
                            val isWinStep = equityPoints[i] >= equityPoints[i - 1]
                            drawCircle(
                                color = if (isWinStep) NeonGreen else NeonRed,
                                radius = 4f,
                                center = pt
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Peak: $${String.format("%,.0f", maxEq)}", fontSize = 10.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                Text("Valley: $${String.format("%,.0f", minEq)}", fontSize = 10.sp, color = NeonRed, fontWeight = FontWeight.SemiBold)
                Text("Current Equity: $${String.format("%,.2f", running)}", fontSize = 10.sp, color = KellyGold, fontWeight = FontWeight.Bold)
            }
        }
    }
}

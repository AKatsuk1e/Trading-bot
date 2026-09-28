package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Candle
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(DarkSurface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading live market candles...", color = TextSecondary, fontSize = 12.sp)
        }
        return
    }

    val displayCandles = candles.takeLast(35)
    val maxPrice = displayCandles.maxOfOrNull { it.high } ?: 100.0
    val minPrice = displayCandles.minOfOrNull { it.low } ?: 90.0
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.0001)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "15m Live Candlesticks",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "High: $${String.format("%.2f", maxPrice)}",
                fontSize = 11.sp,
                color = NeonGreen,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
            Text(
                text = "Low: $${String.format("%.2f", minPrice)}",
                fontSize = 11.sp,
                color = NeonRed,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val width = size.width
            val height = size.height
            val candleCount = displayCandles.size
            val candleWidth = (width / candleCount.toFloat()) * 0.7f
            val spacing = width / candleCount.toFloat()

            // Horizontal grid lines
            val gridSteps = 3
            for (i in 0..gridSteps) {
                val y = height * (i.toFloat() / gridSteps.toFloat())
                drawLine(
                    color = DarkBorder.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Draw candles
            val smaPoints = mutableListOf<Offset>()

            displayCandles.forEachIndexed { index, candle ->
                val xCenter = (index * spacing) + (spacing / 2f)

                val highY = (height - ((candle.high - minPrice) / priceRange * height)).toFloat()
                val lowY = (height - ((candle.low - minPrice) / priceRange * height)).toFloat()
                val openY = (height - ((candle.open - minPrice) / priceRange * height)).toFloat()
                val closeY = (height - ((candle.close - minPrice) / priceRange * height)).toFloat()

                val isGreen = candle.close >= candle.open
                val candleColor = if (isGreen) NeonGreen else NeonRed

                // Draw Wick
                drawLine(
                    color = candleColor,
                    start = Offset(xCenter, highY),
                    end = Offset(xCenter, lowY),
                    strokeWidth = 1.8f,
                    cap = StrokeCap.Round
                )

                // Draw Body
                val bodyTop = minOf(openY, closeY)
                val bodyHeight = maxOf(2f, kotlin.math.abs(closeY - openY))
                drawRect(
                    color = candleColor,
                    topLeft = Offset(xCenter - (candleWidth / 2f), bodyTop),
                    size = Size(candleWidth, bodyHeight)
                )

                smaPoints.add(Offset(xCenter, (openY + closeY) / 2f))
            }

            // Draw smoothed trend line
            if (smaPoints.size > 2) {
                val path = Path()
                path.moveTo(smaPoints.first().x, smaPoints.first().y)
                for (i in 1 until smaPoints.size) {
                    path.lineTo(smaPoints[i].x, smaPoints[i].y)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF00E5FF).copy(alpha = 0.45f),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Past 35 periods (15m)", fontSize = 10.sp, color = TextMuted)
            Spacer(modifier = Modifier.weight(1f))
            Text("SMA Overlay (Cyan)", fontSize = 10.sp, color = Color(0xFF00E5FF).copy(alpha = 0.7f))
        }
    }
}

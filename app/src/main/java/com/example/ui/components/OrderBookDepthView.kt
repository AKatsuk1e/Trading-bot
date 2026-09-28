package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderBookDepth
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OrderBookDepthView(
    depth: OrderBookDepth?,
    modifier: Modifier = Modifier
) {
    if (depth == null) return

    val bidPct = (depth.bidVolumeRatio * 100).toInt()
    val askPct = 100 - bidPct

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
                text = "Order Book Depth (L2)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$bidPct% Bids vs $askPct% Asks",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (bidPct >= 50) NeonGreen else NeonRed
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ratio Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((depth.bidVolumeRatio.toFloat()).coerceIn(0.05f, 0.95f))
                    .background(NeonGreen)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((1f - depth.bidVolumeRatio.toFloat()).coerceIn(0.05f, 0.95f))
                    .background(NeonRed)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Order Book Table (Bids on left, Asks on right)
        Row(modifier = Modifier.fillMaxWidth()) {
            // Top 3 Bids
            Column(modifier = Modifier.weight(1f)) {
                Text("BUY BIDS (USD / QTY)", fontSize = 10.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                depth.bids.take(3).forEach { (p, q) ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(String.format("%.2f", p), fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(String.format("%.3f", q), fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            // Top 3 Asks
            Column(modifier = Modifier.weight(1f)) {
                Text("SELL ASKS (USD / QTY)", fontSize = 10.sp, color = NeonRed, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                depth.asks.take(3).forEach { (p, q) ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(String.format("%.2f", p), fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(String.format("%.3f", q), fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

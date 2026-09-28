package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgentReport
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AgentDebateCard(
    bullReport: AgentReport?,
    bearReport: AgentReport?,
    modifier: Modifier = Modifier
) {
    val bullConf = bullReport?.confidence ?: 0.65
    val bearConf = bearReport?.confidence ?: 0.35
    val winner = if (bullConf >= bearConf) "BULL ADVOCATE" else "BEAR ADVOCATE"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(DarkSurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Balance,
                    contentDescription = "Research Debate",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
            Column {
                Text(
                    text = "Research Debate Chamber",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Bull Advocate vs Bear Advocate Cross-Examination",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(if (bullConf >= bearConf) NeonGreen.copy(alpha = 0.15f) else NeonRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Lead: $winner",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (bullConf >= bearConf) NeonGreen else NeonRed
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Debate Meter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                Text("Bull ${(bullConf * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bear ${(bearConf * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonRed)
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = NeonRed, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (bullConf.toFloat() / (bullConf + bearConf).toFloat()).coerceIn(0.05f, 0.95f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = NeonGreen,
            trackColor = NeonRed
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Two Column arguments
        Row(modifier = Modifier.fillMaxWidth()) {
            // Bullish Argument Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .border(1.dp, NeonGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text("BULL THESIS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                Spacer(modifier = Modifier.height(4.dp))
                bullReport?.keyPoints?.take(3)?.forEach { pt ->
                    Text("• $pt", fontSize = 10.sp, color = TextSecondary, lineHeight = 13.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            Spacer(modifier = Modifier.padding(horizontal = 5.dp))

            // Bearish Argument Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .border(1.dp, NeonRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text("BEAR DEFENSE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonRed)
                Spacer(modifier = Modifier.height(4.dp))
                bearReport?.keyPoints?.take(3)?.forEach { pt ->
                    Text("• $pt", fontSize = 10.sp, color = TextSecondary, lineHeight = 13.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

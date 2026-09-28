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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.FullAnalysisResult
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
fun KellyMathCard(
    analysis: FullAnalysisResult?,
    portfolioEquity: Double,
    modifier: Modifier = Modifier
) {
    val traderReport = analysis?.traderReport
    val winProbStr = traderReport?.metrics?.get("Win_Prob") ?: "58.0%"
    val kellyQuarterStr = traderReport?.metrics?.get("Kelly_Quarter_Pct") ?: "2.40%"
    val positionUsdStr = traderReport?.metrics?.get("Position_Usd") ?: "$240.00"
    val isCapped = traderReport?.metrics?.get("Is_Capped")?.toBoolean() ?: false
    val action = traderReport?.metrics?.get("Action") ?: "HOLD"

    val kellyPctVal = kellyQuarterStr.replace("%", "").toDoubleOrNull() ?: 2.4
    val winProbVal = winProbStr.replace("%", "").toDoubleOrNull() ?: 58.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, KellyGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(KellyGold.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "Kelly Math",
                    tint = KellyGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
            Column {
                Text(
                    text = "Kelly Criterion Math (1/4 Kelly Method)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = KellyGold
                )
                Text(
                    text = "Optimal sizing with quadratic risk reduction",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Math formula box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "Formula:  K% = [(b · p - q) / b] × 0.25",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "where: b = 2.50 (odds/ratio), p = ${winProbStr} (win prob), q = ${(100.0 - winProbVal).toInt()}% (loss prob)",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Metrics Grid
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("1/4 KELLY ALLOCATION", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Text(kellyQuarterStr, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = KellyGold)
                Text(positionUsdStr, fontSize = 11.sp, color = TextSecondary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("WIN PROBABILITY (p)", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Text(winProbStr, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (winProbVal >= 55) NeonGreen else NeonRed)
                Text("Consensus edge", fontSize = 11.sp, color = TextSecondary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("MAX RISK CEILING", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Text("6.00% MAX", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00E5FF))
                Text(if (isCapped) "Capped at 6%" else "Safe within limit", fontSize = 11.sp, color = if (isCapped) NeonRed else NeonGreen)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual progress meter of Kelly % towards 6% limit
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Position Exposure (${String.format("%.1f", (kellyPctVal / 6.0 * 100).coerceIn(0.0, 100.0))}%)", fontSize = 10.sp, color = TextMuted)
                Spacer(modifier = Modifier.weight(1f))
                Text("Target: +5.0% TP / -2.0% SL", fontSize = 10.sp, color = NeonGreen)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (kellyPctVal.toFloat() / 6.0f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = KellyGold,
                trackColor = DarkBorder
            )
        }
    }
}

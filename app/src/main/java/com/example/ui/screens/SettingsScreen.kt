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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TradingViewModel
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
fun SettingsScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val botConfig by viewModel.botConfig.collectAsStateWithLifecycle()
    val geminiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val exchangeKey by viewModel.exchangeApiKey.collectAsStateWithLifecycle()
    val exchangeSecret by viewModel.exchangeApiSecret.collectAsStateWithLifecycle()

    var isRealMoney by remember { mutableStateOf(botConfig.isRealMoneyMode) }
    var selectedExchange by remember { mutableStateOf(botConfig.exchangeName) }
    var inputExchangeKey by remember { mutableStateOf(exchangeKey) }
    var inputExchangeSecret by remember { mutableStateOf(exchangeSecret) }
    var showSecret by remember { mutableStateOf(false) }

    var inputGeminiKey by remember { mutableStateOf(geminiKey) }
    var kellyFractionVal by remember { mutableStateOf(botConfig.kellyFraction.toFloat()) }
    var maxRiskVal by remember { mutableStateOf((botConfig.maxPositionPct * 100).toFloat()) }
    var savedFeedback by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Trading & Exchange Settings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Configure live money trading, Gemini AI keys, and risk parameters",
            fontSize = 11.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Trading Mode Card (Paper vs Real Money)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isRealMoney) NeonRed else DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = if (isRealMoney) NeonRed else NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isRealMoney) "Live Real Money Exchange Mode" else "Paper Trading (Simulation Mode)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRealMoney) NeonRed else NeonGreen
                        )
                        Text(
                            text = if (isRealMoney) "Real order placement via exchange API keys" else "Risk-free simulation with $10,000 USDT virtual equity",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isRealMoney,
                        onCheckedChange = { isRealMoney = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonRed,
                            uncheckedThumbColor = Color.Black,
                            uncheckedTrackColor = NeonGreen
                        ),
                        modifier = Modifier.testTag("real_money_toggle")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Exchange API Keys Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = KellyGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exchange API Credentials", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exchange Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Binance", "Bybit", "Coinbase Pro").forEach { ex ->
                        val isSel = selectedExchange == ex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) KellyGold.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .border(1.dp, if (isSel) KellyGold else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedExchange = ex }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(ex, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) KellyGold else TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputExchangeKey,
                    onValueChange = { inputExchangeKey = it },
                    label = { Text("$selectedExchange API Key", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("exchange_api_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KellyGold,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputExchangeSecret,
                    onValueChange = { inputExchangeSecret = it },
                    label = { Text("$selectedExchange Secret Key", fontSize = 11.sp) },
                    visualTransformation = if (showSecret) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showSecret = !showSecret }) {
                            Icon(
                                imageVector = if (showSecret) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("exchange_secret_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KellyGold,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.setExchangeCredentials(inputExchangeKey, inputExchangeSecret, selectedExchange, isRealMoney)
                        savedFeedback = "Exchange credentials saved for $selectedExchange!"
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("save_exchange_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = KellyGold)
                ) {
                    Text("Save & Connect $selectedExchange", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Gemini AI API Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gemini AI Reasoning Engine", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Used for real-time macro sentiment evaluation and multi-agent synthesis using gemini-3.5-flash.",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputGeminiKey,
                    onValueChange = { inputGeminiKey = it },
                    label = { Text("Gemini API Key (Optional)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("gemini_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.setGeminiApiKey(inputGeminiKey)
                        savedFeedback = "Gemini API key saved."
                    },
                    modifier = Modifier.fillMaxWidth().height(38.dp).testTag("save_gemini_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.2f))
                ) {
                    Text("Apply Gemini Key", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Risk & Kelly Parameters Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kelly Criterion & Risk Management Limits", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Kelly Fraction Slider
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Kelly Fraction Multiplier", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = when {
                            kellyFractionVal <= 0.28f -> "1/4 Kelly (0.25 - Recommended)"
                            kellyFractionVal <= 0.52f -> "1/2 Kelly (0.50)"
                            else -> "Full Kelly (1.00)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KellyGold
                    )
                }
                Slider(
                    value = kellyFractionVal,
                    onValueChange = { kellyFractionVal = it },
                    valueRange = 0.1f..1.0f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = KellyGold, activeTrackColor = KellyGold)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Max Risk Ceiling
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Maximum Trade Allocation Ceiling", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${maxRiskVal.toInt()}% MAX", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                }
                Slider(
                    value = maxRiskVal,
                    onValueChange = { maxRiskVal = it },
                    valueRange = 2f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val newCfg = botConfig.copy(
                            kellyFraction = kellyFractionVal.toDouble(),
                            maxPositionPct = maxRiskVal.toDouble() / 100.0
                        )
                        viewModel.updateConfig(newCfg)
                        savedFeedback = "Risk parameters updated successfully!"
                    },
                    modifier = Modifier.fillMaxWidth().height(38.dp).testTag("save_risk_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.2f))
                ) {
                    Text("Save Risk Parameters", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reset Portfolio Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Paper Portfolio", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Clears trade history and resets paper equity back to $10,000.00 USDT.", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.resetPortfolio()
                        savedFeedback = "Paper portfolio reset to $10,000.00 USDT!"
                    },
                    modifier = Modifier.fillMaxWidth().height(38.dp).testTag("reset_portfolio_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Text("Reset to $10,000 USDT", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        if (savedFeedback != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(savedFeedback ?: "", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

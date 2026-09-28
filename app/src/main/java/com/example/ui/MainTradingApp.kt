package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AgentBotScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignalsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.KellyGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class TradingNavTab(val title: String, val icon: ImageVector, val tag: String) {
    MARKET("Market", Icons.AutoMirrored.Filled.TrendingUp, "nav_market"),
    BOT("AI Bot", Icons.Default.Psychology, "nav_bot"),
    SIGNALS("Signals", Icons.Default.Insights, "nav_signals"),
    PORTFOLIO("Portfolio", Icons.Default.AccountBalanceWallet, "nav_portfolio"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTradingApp(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(TradingNavTab.MARKET) }
    val isAutoRunning by viewModel.isAutonomousRunning.collectAsStateWithLifecycle()
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    val botConfig by viewModel.botConfig.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(KellyGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("K", fontWeight = FontWeight.Black, color = KellyGold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "KellyTrade AI",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isAutoRunning) NeonGreen else Color(0xFF00E5FF), CircleShape)
                                )
                            }
                            Text(
                                text = if (botConfig.isRealMoneyMode) "REAL EXCHANGE • $${String.format("%,.0f", portfolio.totalEquity)}" else "PAPER 1/4 KELLY • $${String.format("%,.0f", portfolio.totalEquity)}",
                                fontSize = 10.sp,
                                color = if (botConfig.isRealMoneyMode) KellyGold else TextMuted
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = if (isAutoRunning) "BOT ON" else "BOT OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAutoRunning) NeonGreen else TextMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isAutoRunning,
                            onCheckedChange = { viewModel.toggleAutonomousTrading(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = NeonGreen,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkBorder
                            ),
                            modifier = Modifier.testTag("appbar_bot_switch")
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                TradingNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (tab == TradingNavTab.BOT && isAutoRunning) NeonGreen else Color(0xFF00E5FF),
                            selectedTextColor = if (tab == TradingNavTab.BOT && isAutoRunning) NeonGreen else Color(0xFF00E5FF),
                            indicatorColor = (if (tab == TradingNavTab.BOT && isAutoRunning) NeonGreen else Color(0xFF00E5FF)).copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "ScreenTransition") { target ->
                when (target) {
                    TradingNavTab.MARKET -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToBot = { currentTab = TradingNavTab.BOT }
                    )
                    TradingNavTab.BOT -> AgentBotScreen(
                        viewModel = viewModel
                    )
                    TradingNavTab.SIGNALS -> SignalsScreen(
                        viewModel = viewModel
                    )
                    TradingNavTab.PORTFOLIO -> PortfolioScreen(
                        viewModel = viewModel
                    )
                    TradingNavTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

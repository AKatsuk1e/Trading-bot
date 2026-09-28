package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.Candle
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
import org.json.JSONArray
import org.json.JSONObject

enum class D3ChartMode {
    LIVE_CANDLESTICKS,
    TRADE_PERFORMANCE_EQUITY
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3FinancialChartView(
    candles: List<Candle>,
    symbol: String,
    trades: List<Trade>,
    initialEquity: Double = 10000.0,
    modifier: Modifier = Modifier
) {
    var chartMode by remember { mutableStateOf(D3ChartMode.LIVE_CANDLESTICKS) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with Chart Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(KellyGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("D3", fontSize = 11.sp, fontWeight = FontWeight.Black, color = KellyGold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (chartMode == D3ChartMode.LIVE_CANDLESTICKS) "$symbol Live Action (D3.js)" else "Historical Trade Performance (D3.js)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (chartMode == D3ChartMode.LIVE_CANDLESTICKS) "Interactive SVG with touch crosshair" else "Cumulative equity & Kelly win/loss tracking",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                // Mode switch pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartMode == D3ChartMode.LIVE_CANDLESTICKS) Color(0xFF00E5FF).copy(alpha = 0.2f) else DarkSurfaceVariant)
                            .border(1.dp, if (chartMode == D3ChartMode.LIVE_CANDLESTICKS) Color(0xFF00E5FF) else DarkBorder, RoundedCornerShape(6.dp))
                            .clickable { chartMode = D3ChartMode.LIVE_CANDLESTICKS }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CandlestickChart, contentDescription = null, tint = if (chartMode == D3ChartMode.LIVE_CANDLESTICKS) Color(0xFF00E5FF) else TextMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Candles", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (chartMode == D3ChartMode.LIVE_CANDLESTICKS) Color(0xFF00E5FF) else TextSecondary)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartMode == D3ChartMode.TRADE_PERFORMANCE_EQUITY) KellyGold.copy(alpha = 0.2f) else DarkSurfaceVariant)
                            .border(1.dp, if (chartMode == D3ChartMode.TRADE_PERFORMANCE_EQUITY) KellyGold else DarkBorder, RoundedCornerShape(6.dp))
                            .clickable { chartMode = D3ChartMode.TRADE_PERFORMANCE_EQUITY }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = if (chartMode == D3ChartMode.TRADE_PERFORMANCE_EQUITY) KellyGold else TextMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Equity Curve", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (chartMode == D3ChartMode.TRADE_PERFORMANCE_EQUITY) KellyGold else TextSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Embedded Interactive D3/SVG Web Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090D14))
            ) {
                val htmlContent = remember(candles, symbol, trades, chartMode, initialEquity) {
                    if (chartMode == D3ChartMode.LIVE_CANDLESTICKS) {
                        generateCandlestickD3Html(candles, symbol)
                    } else {
                        generatePerformanceD3Html(trades, initialEquity)
                    }
                }

                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                            setBackgroundColor(android.graphics.Color.parseColor("#090D14"))
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                cacheMode = WebSettings.LOAD_NO_CACHE
                            }
                            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (chartMode == D3ChartMode.LIVE_CANDLESTICKS) "Drag or touch anywhere on the chart for live crosshair & OHLC stats" else "Touch equity plot nodes to inspect historical trade PnL & Kelly fractions",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * Generates an interactive self-contained D3.js/SVG financial candlestick chart.
 */
private fun generateCandlestickD3Html(candles: List<Candle>, symbol: String): String {
    val displayCandles = candles.takeLast(35)
    val jsonArray = JSONArray()
    displayCandles.forEach { c ->
        val obj = JSONObject().apply {
            put("t", c.timestamp)
            put("o", c.open)
            put("h", c.high)
            put("l", c.low)
            put("c", c.close)
            put("v", c.volume)
        }
        jsonArray.put(obj)
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
              background-color: #090D14;
              color: #F1F5F9;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace;
              overflow: hidden;
              user-select: none;
              -webkit-user-select: none;
            }
            #hud {
              position: absolute;
              top: 6px;
              left: 10px;
              right: 10px;
              display: flex;
              justify-content: space-between;
              font-size: 10px;
              color: #94A3B8;
              pointer-events: none;
              z-index: 10;
            }
            .hud-val { font-weight: bold; color: #F1F5F9; }
            .green { color: #00E676; }
            .red { color: #FF3366; }
            .cyan { color: #00E5FF; }
            svg { width: 100vw; height: 100vh; display: block; }
            .grid-line { stroke: #1A2230; stroke-width: 1; stroke-dasharray: 2,2; }
            .sma-line { fill: none; stroke: #00E5FF; stroke-width: 1.5; opacity: 0.85; }
            .crosshair { stroke: #64748B; stroke-width: 1; stroke-dasharray: 3,3; }
            .vol-bar { opacity: 0.35; }
          </style>
        </head>
        <body>
          <div id="hud">
            <div>
              <span>$symbol </span>
              <span id="stat-ohlc">O: -- H: -- L: -- C: --</span>
            </div>
            <div>
              <span class="cyan">SMA(20)</span>
            </div>
          </div>
          <svg id="chart"></svg>

          <script>
            const data = $jsonArray;
            const svg = document.getElementById('chart');
            const ohlcText = document.getElementById('stat-ohlc');

            function render() {
              const width = window.innerWidth;
              const height = window.innerHeight;
              svg.setAttribute('viewBox', `0 0 ${'$'}{width} ${'$'}{height}`);
              svg.innerHTML = '';

              if (!data || data.length === 0) return;

              const padTop = 26;
              const padBottom = 22;
              const padRight = 36;
              const padLeft = 4;
              const plotW = width - padLeft - padRight;
              const plotH = height - padTop - padBottom;
              const priceH = plotH * 0.75;
              const volH = plotH * 0.22;
              const volTop = padTop + priceH + 6;

              const highs = data.map(d => d.h);
              const lows = data.map(d => d.l);
              const minPrice = Math.min(...lows);
              const maxPrice = Math.max(...highs);
              const priceSpan = (maxPrice - minPrice) || 1;

              const maxVol = Math.max(...data.map(d => d.v)) || 1;

              // Grid lines
              for (let i = 0; i <= 3; i++) {
                const y = padTop + (priceH * (i / 3));
                const priceVal = maxPrice - (priceSpan * (i / 3));
                const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                line.setAttribute('x1', padLeft);
                line.setAttribute('x2', width - padRight);
                line.setAttribute('y1', y);
                line.setAttribute('y2', y);
                line.setAttribute('class', 'grid-line');
                svg.appendChild(line);

                const label = document.createElementNS('http://www.w3.org/2000/svg', 'text');
                label.setAttribute('x', width - padRight + 4);
                label.setAttribute('y', y + 3);
                label.setAttribute('fill', '#64748B');
                label.setAttribute('font-size', '9px');
                label.textContent = priceVal.toFixed(1);
                svg.appendChild(label);
              }

              const n = data.length;
              const step = plotW / n;
              const candleW = Math.max(3, step * 0.65);

              // Draw Candlesticks & Volumes
              const smaPoints = [];
              data.forEach((d, i) => {
                const x = padLeft + (i * step) + (step / 2);
                const isGreen = d.c >= d.o;
                const color = isGreen ? '#00E676' : '#FF3366';

                const highY = padTop + ((maxPrice - d.h) / priceSpan * priceH);
                const lowY = padTop + ((maxPrice - d.l) / priceSpan * priceH);
                const openY = padTop + ((maxPrice - d.o) / priceSpan * priceH);
                const closeY = padTop + ((maxPrice - d.c) / priceSpan * priceH);

                // Wick
                const wick = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                wick.setAttribute('x1', x);
                wick.setAttribute('x2', x);
                wick.setAttribute('y1', highY);
                wick.setAttribute('y2', lowY);
                wick.setAttribute('stroke', color);
                wick.setAttribute('stroke-width', '1.5');
                svg.appendChild(wick);

                // Body
                const bodyTop = Math.min(openY, closeY);
                const bodyH = Math.max(2, Math.abs(closeY - openY));
                const body = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
                body.setAttribute('x', x - (candleW / 2));
                body.setAttribute('y', bodyTop);
                body.setAttribute('width', candleW);
                body.setAttribute('height', bodyH);
                body.setAttribute('fill', color);
                svg.appendChild(body);

                // Volume Bar
                const vBarH = (d.v / maxVol) * volH;
                const vBar = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
                vBar.setAttribute('x', x - (candleW / 2));
                vBar.setAttribute('y', volTop + (volH - vBarH));
                vBar.setAttribute('width', candleW);
                vBar.setAttribute('height', vBarH);
                vBar.setAttribute('fill', color);
                vBar.setAttribute('class', 'vol-bar');
                svg.appendChild(vBar);

                // SMA 10/20 calculation
                if (i >= 5) {
                  const slice = data.slice(Math.max(0, i - 9), i + 1);
                  const avg = slice.reduce((acc, v) => acc + v.c, 0) / slice.length;
                  const smaY = padTop + ((maxPrice - avg) / priceSpan * priceH);
                  smaPoints.push({ x, y: smaY });
                }
              });

              // SMA line
              if (smaPoints.length > 1) {
                let pathD = `M ${'$'}{smaPoints[0].x} ${'$'}{smaPoints[0].y}`;
                for (let k = 1; k < smaPoints.length; k++) {
                  pathD += ` L ${'$'}{smaPoints[k].x} ${'$'}{smaPoints[k].y}`;
                }
                const smaPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
                smaPath.setAttribute('d', pathD);
                smaPath.setAttribute('class', 'sma-line');
                svg.appendChild(smaPath);
              }

              // Update default HUD with latest candle
              const latest = data[data.length - 1];
              if (latest) {
                updateHud(latest);
              }

              // Touch Crosshair
              let vCross = null;
              let hCross = null;

              function handleTouch(evt) {
                const rect = svg.getBoundingClientRect();
                const clientX = (evt.touches ? evt.touches[0].clientX : evt.clientX) - rect.left;
                const idx = Math.min(n - 1, Math.max(0, Math.floor((clientX - padLeft) / step)));
                const d = data[idx];
                if (!d) return;

                const x = padLeft + (idx * step) + (step / 2);
                const closeY = padTop + ((maxPrice - d.c) / priceSpan * priceH);

                if (!vCross) {
                  vCross = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                  vCross.setAttribute('class', 'crosshair');
                  svg.appendChild(vCross);
                }
                if (!hCross) {
                  hCross = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                  hCross.setAttribute('class', 'crosshair');
                  svg.appendChild(hCross);
                }

                vCross.setAttribute('x1', x);
                vCross.setAttribute('x2', x);
                vCross.setAttribute('y1', padTop);
                vCross.setAttribute('y2', padTop + plotH);

                hCross.setAttribute('x1', padLeft);
                hCross.setAttribute('x2', width - padRight);
                hCross.setAttribute('y1', closeY);
                hCross.setAttribute('y2', closeY);

                updateHud(d);
              }

              svg.addEventListener('mousemove', handleTouch);
              svg.addEventListener('touchmove', handleTouch, { passive: true });
              svg.addEventListener('touchstart', handleTouch, { passive: true });
            }

            function updateHud(d) {
              const col = d.c >= d.o ? 'green' : 'red';
              ohlcText.innerHTML = `O:<span class="hud-val">${'$'}{d.o.toFixed(2)}</span> H:<span class="hud-val">${'$'}{d.h.toFixed(2)}</span> L:<span class="hud-val">${'$'}{d.l.toFixed(2)}</span> C:<span class="${'$'}{col} hud-val">${'$'}{d.c.toFixed(2)}</span>`;
            }

            window.addEventListener('resize', render);
            render();
          </script>
        </body>
        </html>
    """.trimIndent()
}

/**
 * Generates an interactive D3.js/SVG historical trade performance equity curve.
 */
private fun generatePerformanceD3Html(trades: List<Trade>, initialEquity: Double): String {
    // Generate equity milestones from trade list
    val jsonTrades = JSONArray()
    var cumulativeEquity = initialEquity
    val baselineObj = JSONObject().apply {
        put("idx", 0)
        put("equity", cumulativeEquity)
        put("pnl", 0.0)
        put("pnlPct", 0.0)
        put("symbol", "START")
        put("side", "CASH")
        put("isWin", true)
        put("kelly", 0.25)
    }
    jsonTrades.put(baselineObj)

    val closedList = trades.filter { it.status.name != "OPEN" }

    if (closedList.isEmpty()) {
        // Sample demonstration milestones if no trades yet completed
        val demoPnl = listOf(142.50, -85.20, 260.40, 195.00, -62.00, 310.80, 185.00)
        demoPnl.forEachIndexed { i, p ->
            cumulativeEquity += p
            val pct = (p / 250.0) * 100.0
            val obj = JSONObject().apply {
                put("idx", i + 1)
                put("equity", cumulativeEquity)
                put("pnl", p)
                put("pnlPct", pct)
                put("symbol", if (i % 2 == 0) "BTCUSDT" else "ETHUSDT")
                put("side", if (p >= 0) "BUY" else "SELL")
                put("isWin", p >= 0)
                put("kelly", 0.25)
            }
            jsonTrades.put(obj)
        }
    } else {
        closedList.forEachIndexed { i, t ->
            cumulativeEquity += t.pnl
            val obj = JSONObject().apply {
                put("idx", i + 1)
                put("equity", cumulativeEquity)
                put("pnl", t.pnl)
                put("pnlPct", t.pnlPercent)
                put("symbol", t.symbol)
                put("side", t.side.name)
                put("isWin", t.pnl >= 0)
                put("kelly", t.kellyFraction)
            }
            jsonTrades.put(obj)
        }
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
              background-color: #090D14;
              color: #F1F5F9;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace;
              overflow: hidden;
            }
            #hud {
              position: absolute;
              top: 6px;
              left: 10px;
              right: 10px;
              display: flex;
              justify-content: space-between;
              font-size: 10px;
              color: #94A3B8;
              z-index: 10;
              pointer-events: none;
            }
            .gold { color: #FFD700; font-weight: bold; }
            .green { color: #00E676; font-weight: bold; }
            .red { color: #FF3366; font-weight: bold; }
            svg { width: 100vw; height: 100vh; display: block; }
            .grid-line { stroke: #1A2230; stroke-width: 1; stroke-dasharray: 2,2; }
            .equity-line { fill: none; stroke: #FFD700; stroke-width: 2.5; stroke-linecap: round; }
            .crosshair { stroke: #64748B; stroke-width: 1; stroke-dasharray: 3,3; }
          </style>
        </head>
        <body>
          <div id="hud">
            <div>
              <span>Portfolio Equity: </span>
              <span id="hud-equity" class="gold">$10,000.00</span>
            </div>
            <div>
              <span id="hud-trade">Trade: --</span>
            </div>
          </div>
          <svg id="chart"></svg>

          <script>
            const data = $jsonTrades;
            const svg = document.getElementById('chart');
            const hudEquity = document.getElementById('hud-equity');
            const hudTrade = document.getElementById('hud-trade');

            function render() {
              const width = window.innerWidth;
              const height = window.innerHeight;
              svg.setAttribute('viewBox', `0 0 ${'$'}{width} ${'$'}{height}`);
              svg.innerHTML = '';

              if (!data || data.length === 0) return;

              const padTop = 28;
              const padBottom = 20;
              const padLeft = 8;
              const padRight = 50;
              const plotW = width - padLeft - padRight;
              const plotH = height - padTop - padBottom;

              const equities = data.map(d => d.equity);
              let minEq = Math.min(...equities) * 0.995;
              let maxEq = Math.max(...equities) * 1.005;
              if (maxEq === minEq) { maxEq += 100; minEq -= 100; }
              const eqSpan = maxEq - minEq;

              // Grid lines
              for (let i = 0; i <= 3; i++) {
                const y = padTop + (plotH * (i / 3));
                const eqVal = maxEq - (eqSpan * (i / 3));
                const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                line.setAttribute('x1', padLeft);
                line.setAttribute('x2', width - padRight);
                line.setAttribute('y1', y);
                line.setAttribute('y2', y);
                line.setAttribute('class', 'grid-line');
                svg.appendChild(line);

                const label = document.createElementNS('http://www.w3.org/2000/svg', 'text');
                label.setAttribute('x', width - padRight + 4);
                label.setAttribute('y', y + 3);
                label.setAttribute('fill', '#94A3B8');
                label.setAttribute('font-size', '9px');
                label.textContent = '$' + eqVal.toFixed(0);
                svg.appendChild(label);
              }

              const n = data.length;
              const step = plotW / Math.max(1, (n - 1));

              // Compute points
              const pts = data.map((d, i) => {
                const x = padLeft + (i * step);
                const y = padTop + ((maxEq - d.equity) / eqSpan * plotH);
                return { x, y, data: d };
              });

              // Area fill gradient
              const defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs');
              const grad = document.createElementNS('http://www.w3.org/2000/svg', 'linearGradient');
              grad.setAttribute('id', 'equityGrad');
              grad.setAttribute('x1', '0');
              grad.setAttribute('y1', '0');
              grad.setAttribute('x2', '0');
              grad.setAttribute('y2', '1');

              const stop1 = document.createElementNS('http://www.w3.org/2000/svg', 'stop');
              stop1.setAttribute('offset', '0%');
              stop1.setAttribute('stop-color', '#FFD700');
              stop1.setAttribute('stop-opacity', '0.25');

              const stop2 = document.createElementNS('http://www.w3.org/2000/svg', 'stop');
              stop2.setAttribute('offset', '100%');
              stop2.setAttribute('stop-color', '#FFD700');
              stop2.setAttribute('stop-opacity', '0.0');

              grad.appendChild(stop1);
              grad.appendChild(stop2);
              defs.appendChild(grad);
              svg.appendChild(defs);

              // Area path
              let areaD = `M ${'$'}{pts[0].x} ${'$'}{padTop + plotH} L ${'$'}{pts[0].x} ${'$'}{pts[0].y}`;
              for (let i = 1; i < pts.length; i++) {
                areaD += ` L ${'$'}{pts[i].x} ${'$'}{pts[i].y}`;
              }
              areaD += ` L ${'$'}{pts[pts.length - 1].x} ${'$'}{padTop + plotH} Z`;

              const areaPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
              areaPath.setAttribute('d', areaD);
              areaPath.setAttribute('fill', 'url(#equityGrad)');
              svg.appendChild(areaPath);

              // Equity curve line
              let lineD = `M ${'$'}{pts[0].x} ${'$'}{pts[0].y}`;
              for (let i = 1; i < pts.length; i++) {
                lineD += ` L ${'$'}{pts[i].x} ${'$'}{pts[i].y}`;
              }
              const eqLine = document.createElementNS('http://www.w3.org/2000/svg', 'path');
              eqLine.setAttribute('d', lineD);
              eqLine.setAttribute('class', 'equity-line');
              svg.appendChild(eqLine);

              // Nodes for each trade
              pts.forEach((pt, idx) => {
                if (idx === 0) return;
                const dot = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
                dot.setAttribute('cx', pt.x);
                dot.setAttribute('cy', pt.y);
                dot.setAttribute('r', '4');
                dot.setAttribute('fill', pt.data.isWin ? '#00E676' : '#FF3366');
                dot.setAttribute('stroke', '#090D14');
                dot.setAttribute('stroke-width', '1.5');
                svg.appendChild(dot);
              });

              // Initial HUD
              const last = data[data.length - 1];
              if (last) {
                updateHud(last);
              }

              // Touch interaction
              function handleTouch(evt) {
                const rect = svg.getBoundingClientRect();
                const clientX = (evt.touches ? evt.touches[0].clientX : evt.clientX) - rect.left;
                const idx = Math.min(n - 1, Math.max(0, Math.round((clientX - padLeft) / step)));
                const d = data[idx];
                if (d) updateHud(d);
              }

              svg.addEventListener('mousemove', handleTouch);
              svg.addEventListener('touchmove', handleTouch, { passive: true });
              svg.addEventListener('touchstart', handleTouch, { passive: true });
            }

            function updateHud(d) {
              hudEquity.textContent = '$' + d.equity.toFixed(2);
              if (d.symbol === 'START') {
                hudTrade.textContent = 'Account Baseline';
              } else {
                const sign = d.pnl >= 0 ? '+' : '';
                const pnlClass = d.pnl >= 0 ? 'green' : 'red';
                hudTrade.innerHTML = `${'$'}{d.side} ${'$'}{d.symbol} <span class="${'$'}{pnlClass}">(${'$'}{sign}$${'$'}{Math.abs(d.pnl).toFixed(2)}) [1/4 Kelly: 25%]</span>`;
              }
            }

            window.addEventListener('resize', render);
            render();
          </script>
        </body>
        </html>
    """.trimIndent()
}

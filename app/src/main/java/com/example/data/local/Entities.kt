package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Trade
import com.example.model.TradeSide
import com.example.model.TradeStatus
import com.example.model.TradingSignal

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val side: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val amount: Double,
    val usdValue: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val status: String,
    val entryTime: Long,
    val exitTime: Long?,
    val kellyFraction: Double,
    val rationale: String
) {
    fun toDomain(): Trade {
        return Trade(
            id = id,
            symbol = symbol,
            side = try { TradeSide.valueOf(side) } catch (_: Exception) { TradeSide.BUY },
            entryPrice = entryPrice,
            exitPrice = exitPrice,
            amount = amount,
            usdValue = usdValue,
            pnl = pnl,
            pnlPercent = pnlPercent,
            status = try { TradeStatus.valueOf(status) } catch (_: Exception) { TradeStatus.OPEN },
            entryTime = entryTime,
            exitTime = exitTime,
            kellyFraction = kellyFraction,
            rationale = rationale
        )
    }

    companion object {
        fun fromDomain(trade: Trade): TradeEntity {
            return TradeEntity(
                id = trade.id,
                symbol = trade.symbol,
                side = trade.side.name,
                entryPrice = trade.entryPrice,
                exitPrice = trade.exitPrice,
                amount = trade.amount,
                usdValue = trade.usdValue,
                pnl = trade.pnl,
                pnlPercent = trade.pnlPercent,
                status = trade.status.name,
                entryTime = trade.entryTime,
                exitTime = trade.exitTime,
                kellyFraction = trade.kellyFraction,
                rationale = trade.rationale
            )
        }
    }
}

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val side: String,
    val confidence: Double,
    val winProbability: Double,
    val winLossRatio: Double,
    val kellyPercentage: Double,
    val entryPrice: Double,
    val targetPrice: Double,
    val stopLossPrice: Double,
    val timestamp: Long,
    val reasoning: String,
    val timeframe: String,
    val status: String
) {
    fun toDomain(): TradingSignal {
        return TradingSignal(
            id = id,
            symbol = symbol,
            side = try { TradeSide.valueOf(side) } catch (_: Exception) { TradeSide.BUY },
            confidence = confidence,
            winProbability = winProbability,
            winLossRatio = winLossRatio,
            kellyPercentage = kellyPercentage,
            entryPrice = entryPrice,
            targetPrice = targetPrice,
            stopLossPrice = stopLossPrice,
            timestamp = timestamp,
            reasoning = reasoning,
            timeframe = timeframe,
            status = status
        )
    }

    companion object {
        fun fromDomain(signal: TradingSignal): SignalEntity {
            return SignalEntity(
                id = signal.id,
                symbol = signal.symbol,
                side = signal.side.name,
                confidence = signal.confidence,
                winProbability = signal.winProbability,
                winLossRatio = signal.winLossRatio,
                kellyPercentage = signal.kellyPercentage,
                entryPrice = signal.entryPrice,
                targetPrice = signal.targetPrice,
                stopLossPrice = signal.stopLossPrice,
                timestamp = signal.timestamp,
                reasoning = signal.reasoning,
                timeframe = signal.timeframe,
                status = signal.status
            )
        }
    }
}

package com.example

import com.example.core.kelly.KellyCriterion
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun kellyCriterion_quarterKellyCalculation_isAccurate() {
        // win probability = 0.60, win/loss ratio = 2.0, q = 0.40
        // full Kelly = (2.0 * 0.60 - 0.40) / 2.0 = (1.20 - 0.40) / 2.0 = 0.80 / 2.0 = 0.40 (40%)
        // quarter Kelly (0.25) = 0.40 * 0.25 = 0.10 (10%)
        val result = KellyCriterion.calculate(
            winProbability = 0.60,
            winLossRatio = 2.0,
            kellyFraction = 0.25
        )

        assertEquals(0.40, result.fullKellyFraction, 0.001)
        assertEquals(10.0, result.kellyPercentage, 0.001)
        assertEquals(0.10, result.quarterKelly, 0.001)
        assertTrue(result.expectedGrowth > 0.0)
    }

    @Test
    fun kellyCriterion_negativeEdge_returnsZeroAllocation() {
        // p = 0.30, b = 1.0 -> full Kelly = (1*0.3 - 0.7)/1 = -0.40 <= 0 -> 0%
        val result = KellyCriterion.calculate(
            winProbability = 0.30,
            winLossRatio = 1.0,
            kellyFraction = 0.25
        )

        assertEquals(0.0, result.kellyPercentage, 0.0001)
        assertEquals(0.0, result.quarterKelly, 0.0001)
    }

    @Test
    fun positionSizing_respectsSixPercentCap() {
        val result = KellyCriterion.calculate(
            winProbability = 0.70,
            winLossRatio = 2.5,
            kellyFraction = 0.25
        )
        // With high win probability, raw quarter Kelly might exceed 6%.
        // The position sizing function must cap at exactly 6.0% maximum.
        val sizing = KellyCriterion.calculatePositionSize(
            portfolioValue = 10000.0,
            currentPrice = 65000.0,
            kellyResult = result,
            maxPositionPct = 0.06
        )

        assertTrue(sizing.positionPercentage <= 6.0)
        assertTrue(sizing.positionValue <= 600.0)
    }
}

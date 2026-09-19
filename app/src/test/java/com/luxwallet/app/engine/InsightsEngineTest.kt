package com.luxwallet.app.engine

import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsEngineTest {

    @Test
    fun categoryUpSignificantly_producesWarning() {
        val insights = InsightsEngine.generate(
            InsightsInput(
                categorySpends = listOf(CategorySpend("Food", currentMonthAmount = 1_180_000, trailingAverageAmount = 1_000_000)),
                discretionaryBudget = 0,
                discretionarySpent = 0,
                cashAllocationPercent = null,
                cashAllocationTargetRange = null
            )
        )
        assertTrue(insights.any { it.message.contains("Food") && it.severity == InsightSeverity.WARNING })
    }

    @Test
    fun categoryDownSignificantly_producesPositiveInsight() {
        val insights = InsightsEngine.generate(
            InsightsInput(
                categorySpends = listOf(CategorySpend("Transport", currentMonthAmount = 750_000, trailingAverageAmount = 1_000_000)),
                discretionaryBudget = 0,
                discretionarySpent = 0,
                cashAllocationPercent = null,
                cashAllocationTargetRange = null
            )
        )
        assertTrue(insights.any { it.message.contains("Transport") && it.severity == InsightSeverity.POSITIVE })
    }

    @Test
    fun discretionaryBudgetUsagePercent_isReported() {
        val insights = InsightsEngine.generate(
            InsightsInput(
                categorySpends = emptyList(),
                discretionaryBudget = 1_000_000,
                discretionarySpent = 620_000,
                cashAllocationPercent = null,
                cashAllocationTargetRange = null
            )
        )
        assertTrue(insights.any { it.message.contains("62%") })
    }

    @Test
    fun cashAboveTargetRange_producesWarning() {
        val insights = InsightsEngine.generate(
            InsightsInput(
                categorySpends = emptyList(),
                discretionaryBudget = 0,
                discretionarySpent = 0,
                cashAllocationPercent = 55.0,
                cashAllocationTargetRange = 10.0..40.0
            )
        )
        assertTrue(insights.any { it.message.contains("cash allocation") && it.severity == InsightSeverity.WARNING })
    }

    @Test
    fun smallChange_producesNoInsight() {
        val insights = InsightsEngine.generate(
            InsightsInput(
                categorySpends = listOf(CategorySpend("Health", currentMonthAmount = 1_050_000, trailingAverageAmount = 1_000_000)),
                discretionaryBudget = 0,
                discretionarySpent = 0,
                cashAllocationPercent = null,
                cashAllocationTargetRange = null
            )
        )
        assertTrue(insights.none { it.message.contains("Health") })
    }
}

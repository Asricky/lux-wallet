package com.luxwallet.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SafeToSpendEngineTest {

    @Test
    fun basicCalculation_matchesFormula() {
        val result = SafeToSpendEngine.calculate(
            SafeToSpendInput(
                expectedMonthlyIncome = 15_500_000,
                fixedObligations = 5_000_000,
                debtPayments = 0,
                savingsTarget = 2_000_000,
                investmentTarget = 3_000_000,
                plannedExpenses = 0,
                safetyBuffer = 500_000,
                discretionarySpentThisMonthSoFar = 94_500,
                today = LocalDate.of(2026, 9, 18)
            )
        )
        // monthlySpendableBudget = 15.5M - 5M - 0 - 2M - 3M - 0 - 0.5M = 5,000,000
        assertEquals(5_000_000L, result.monthlySpendableBudget)
        assertEquals(4_905_500L, result.remainingSpendableBudget) // 5,000,000 - 94,500
        assertEquals(13, result.remainingDays) // Sep has 30 days; 30-18+1=13
        assertEquals(4_905_500L / 13, result.safeToSpendToday)
        assertFalse(result.isOverBudget)
    }

    @Test
    fun overspending_producesNegativeRemainingAndOverBudgetFlag() {
        val result = SafeToSpendEngine.calculate(
            SafeToSpendInput(
                expectedMonthlyIncome = 5_000_000,
                fixedObligations = 4_000_000,
                debtPayments = 0,
                savingsTarget = 0,
                investmentTarget = 0,
                plannedExpenses = 0,
                safetyBuffer = 0,
                discretionarySpentThisMonthSoFar = 1_500_000, // already over the 1,000,000 monthly spendable budget
                today = LocalDate.of(2026, 9, 20)
            )
        )
        assertEquals(1_000_000L, result.monthlySpendableBudget)
        assertTrue(result.remainingSpendableBudget < 0)
        assertTrue(result.isOverBudget)
    }

    @Test
    fun lastDayOfMonth_remainingDaysIsOne() {
        val result = SafeToSpendEngine.calculate(
            SafeToSpendInput(
                expectedMonthlyIncome = 3_000_000,
                fixedObligations = 0,
                debtPayments = 0,
                savingsTarget = 0,
                investmentTarget = 0,
                plannedExpenses = 0,
                safetyBuffer = 0,
                discretionarySpentThisMonthSoFar = 0,
                today = LocalDate.of(2026, 9, 30)
            )
        )
        assertEquals(1, result.remainingDays)
        assertEquals(3_000_000L, result.safeToSpendToday)
    }
}

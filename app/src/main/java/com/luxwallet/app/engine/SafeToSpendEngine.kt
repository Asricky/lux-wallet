package com.luxwallet.app.engine

import java.time.LocalDate

data class SafeToSpendInput(
    val expectedMonthlyIncome: Long,
    val fixedObligations: Long,
    val debtPayments: Long,
    val savingsTarget: Long,
    val investmentTarget: Long,
    val plannedExpenses: Long,
    val safetyBuffer: Long,
    /** Discretionary (non-fixed, non-excluded) expense total already posted this month, PRD §30. */
    val discretionarySpentThisMonthSoFar: Long,
    val today: LocalDate
)

data class SafeToSpendResult(
    val monthlySpendableBudget: Long,
    val remainingSpendableBudget: Long,
    val remainingDays: Int,
    val safeToSpendToday: Long,
    val isOverBudget: Boolean
)

/**
 * Daily Safe-to-Spend, recalculated continuously (PRD §30). Unused allowance from prior days
 * carries forward automatically because [SafeToSpendResult.remainingSpendableBudget] already
 * nets out everything spent so far this month before dividing by the days left.
 */
object SafeToSpendEngine {
    fun calculate(input: SafeToSpendInput): SafeToSpendResult {
        val monthlySpendableBudget = input.expectedMonthlyIncome -
            input.fixedObligations -
            input.debtPayments -
            input.savingsTarget -
            input.investmentTarget -
            input.plannedExpenses -
            input.safetyBuffer

        val remainingSpendableBudget = monthlySpendableBudget - input.discretionarySpentThisMonthSoFar

        val daysInMonth = input.today.lengthOfMonth()
        val remainingDays = (daysInMonth - input.today.dayOfMonth + 1).coerceAtLeast(1)

        val safeToSpendToday = remainingSpendableBudget / remainingDays

        return SafeToSpendResult(
            monthlySpendableBudget = monthlySpendableBudget,
            remainingSpendableBudget = remainingSpendableBudget,
            remainingDays = remainingDays,
            safeToSpendToday = safeToSpendToday,
            isOverBudget = remainingSpendableBudget < 0
        )
    }
}

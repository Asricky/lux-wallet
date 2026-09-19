package com.luxwallet.app.engine

import com.luxwallet.app.core.common.CashflowMath
import com.luxwallet.app.core.common.TransportPlan
import com.luxwallet.app.core.database.entity.FinancialProfileEntity
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.model.TransactionType
import java.time.LocalDate
import java.time.ZoneId

data class DailyAllowance(
    val income: Long, val otherCommitments: Long, val businessSavings: Long, val investment: Long,
    val transportReserve: Long, val transportSpent: Long, val transportDays: Int,
    val spentBeforeToday: Long, val todaySpent: Long, val remainingDays: Int, val dailyLimit: Long,
    val monthlyBudget: Long
) {
    val remainingToday get() = dailyLimit - todaySpent
    val usedPercent get() = if (dailyLimit > 0) todaySpent.toDouble() / dailyLimit * 100 else if (todaySpent > 0) 100.0 else 0.0
    companion object {
        fun calculate(profile: FinancialProfileEntity, transactions: List<TransactionEntity>, transportIds: Set<Long>,
                      transport: TransportPlan, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): DailyAllowance {
            val start = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val tomorrow = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val todayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
            val expenses = CashflowMath.cashflowEligible(transactions).filter {
                it.direction == TransactionDirection.OUT && it.transactionTime in start until tomorrow
            }
            val transportSpent = expenses.filter { it.categoryId in transportIds }.sumOf { it.amount }
            val freeExpenses = expenses.filter { it.categoryId !in transportIds && it.type != TransactionType.EWALLET_TOPUP }
            val spentBefore = freeExpenses.filter { it.transactionTime < todayStart }.sumOf { it.amount }
            val todaySpent = freeExpenses.filter { it.transactionTime >= todayStart }.sumOf { it.amount }
            val transportReserve = maxOf(transport.monthlyReserve(today), transportSpent)
            val commitments = profile.fixedObligations + profile.debtPayments + profile.plannedExpensesMonthly + profile.safetyBufferMonthly
            val budget = profile.expectedMonthlyIncome - commitments - profile.savingsTargetMonthly - profile.investmentTargetMonthly - transportReserve
            val days = today.lengthOfMonth() - today.dayOfMonth + 1
            return DailyAllowance(profile.expectedMonthlyIncome, commitments, profile.savingsTargetMonthly,
                profile.investmentTargetMonthly, transportReserve, transportSpent, transport.daysInMonth(today),
                spentBefore, todaySpent, days, ((budget - spentBefore) / days).coerceAtLeast(0), budget)
        }
    }
}

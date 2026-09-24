package com.luxwallet.app.engine

import com.luxwallet.app.core.common.CashflowMath
import com.luxwallet.app.core.database.entity.TransactionEntity
import java.time.*
import java.time.temporal.ChronoUnit

data class AdaptiveBudget(val daysRemaining: Int, val cash: Long, val recommended: Long, val current: Long,
    val remaining: Long, val safeToSpend: Long, val totalSpent: Long, val bufferRemaining: Long, val projectedBalance: Long)

object AdaptiveBudgetEngine {
    fun calculate(status: PaydayStatus?, currentCash: Long?, today: LocalDate): AdaptiveBudget? {
        if (status == null || status.expired || today < status.plan.start) return null
        val plan = status.plan
        val days = ChronoUnit.DAYS.between(today, plan.end).toInt().coerceAtLeast(1)
        // The snapshot already includes actual income/expenses. Never add them twice or count forecast pay.
        val cash = minOf(status.cashRemaining, currentCash ?: status.cashRemaining)
        val unpaid = (plan.bills - status.billsSpent).coerceAtLeast(0) + (plan.transportReserve - status.transportSpent).coerceAtLeast(0)
        val protected = unpaid + plan.buffer + plan.business + plan.investment
        val free = (cash - protected).coerceAtLeast(0)
        val recommended = (free + status.spentToday) / days
        val remaining = minOf(status.dailyBudget - status.spentToday, cash - protected)
        val safe = minOf((recommended - status.spentToday).coerceAtLeast(0), remaining.coerceAtLeast(0))
        val plannedSpending = (status.dailyBudget - status.spentToday).coerceAtLeast(0) + status.dailyBudget * (days - 1)
        return AdaptiveBudget(days, cash, recommended, status.dailyBudget, remaining, safe,
            (plan.availableCash + status.actualIncome - status.cashRemaining).coerceAtLeast(0),
            minOf(plan.buffer, (cash - unpaid - plan.business - plan.investment).coerceAtLeast(0)), cash - unpaid - plannedSpending)
    }
}
data class DailyExpense(val date: LocalDate, val amount: Long)
fun expenseTrend(transactions: List<TransactionEntity>, month: YearMonth, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<DailyExpense> {
    val end = minOf(month.atEndOfMonth(), today)
    if (end < month.atDay(1)) return emptyList()
    val grouped = CashflowMath.cashflowEligible(transactions).groupBy { Instant.ofEpochMilli(it.transactionTime).atZone(zone).toLocalDate() }
    return (1..end.dayOfMonth).map { day -> val date = month.atDay(day); DailyExpense(date, CashflowMath.totalExpense(grouped[date].orEmpty())) }
}

package com.luxwallet.app.engine

import com.luxwallet.app.core.common.CashflowMath
import com.luxwallet.app.core.common.TransportPlan
import com.luxwallet.app.core.database.entity.*
import com.luxwallet.app.core.model.*
import java.time.*
import java.time.temporal.ChronoUnit

data class InterimBudget(val until: LocalDate, val cash: Long, val reserved: Long,
    val daily: Long, val spent: Long, val remaining: Long, val days: Int)

/** Read-only bridge to the next scheduled payday; never carries an expired snapshot forward. */
object InterimBudgetEngine {
    fun calculate(plans: List<PaydayPlan>, cash: Long?, transactions: List<TransactionEntity>,
        transportIds: Set<Long>, billIds: Set<Long>, profile: FinancialProfileEntity,
        transport: TransportPlan, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): InterimBudget? {
        if (cash == null || PaydayPlan.forDay(plans, today) != null) return null
        val latest = plans.filter { it.start <= today }.maxByOrNull { it.capturedAt }
        val until = PaydayPlan.nextPayday(today)
        val days = ChronoUnit.DAYS.between(today, until).toInt()
        val eligible = CashflowMath.cashflowEligible(transactions).filter {
            it.type !in setOf(TransactionType.EWALLET_TOPUP, TransactionType.BALANCE_ADJUSTMENT) &&
                Instant.ofEpochMilli(it.transactionTime).atZone(zone).toLocalDate() == today && it.direction == TransactionDirection.OUT
        }
        val spent = eligible.filter { it.categoryId !in transportIds && it.categoryId !in billIds }.sumOf { it.amount }
        val transportDays = (0 until days).count {
            !transport.weekdaysOnly || today.plusDays(it.toLong()).dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        }
        val travel = (transportDays * transport.dailyAmount - eligible.filter { it.categoryId in transportIds }.sumOf { it.amount }).coerceAtLeast(0)
        val previousReserve = latest?.let { it.bills + it.buffer + it.business + it.investment } ?: 0
        val profileReserve = profile.fixedObligations + profile.debtPayments + profile.savingsTargetMonthly +
            profile.investmentTargetMonthly + profile.plannedExpensesMonthly + profile.safetyBufferMonthly
        // Hold the larger full reserve until the user confirms what is paid. Conservative by design.
        val reserved = maxOf(previousReserve, profileReserve) + travel
        val free = (cash - reserved).coerceAtLeast(0)
        val daily = (free + spent) / days
        return InterimBudget(until, cash, reserved, daily, spent, minOf(free, (daily - spent).coerceAtLeast(0)), days)
    }
}

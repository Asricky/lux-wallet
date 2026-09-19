package com.luxwallet.app.engine

import com.luxwallet.app.core.common.TransportPlan
import com.luxwallet.app.core.database.entity.FinancialProfileEntity
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DailyAllowanceTest {
    private val day = LocalDate.of(2026, 9, 19)
    private val zone = ZoneId.of("Asia/Jakarta")
    private val profile = FinancialProfileEntity(baselineDate = 0, expectedMonthlyIncome = 5000000,
        savingsTargetMonthly = 1000000, investmentTargetMonthly = 1500000)
    private fun expense(amount: Long, category: Long, date: LocalDate = day, type: TransactionType = TransactionType.EXPENSE,
        internal: Boolean = false) = TransactionEntity(type = type, direction = TransactionDirection.OUT, amount = amount,
        sourceAccountId = 1, categoryId = category, transactionTime = date.atStartOfDay(zone).toInstant().toEpochMilli(),
        createdAt = 0, updatedAt = 0, confidenceScore = 1.0, reviewStatus = ReviewStatus.CONFIRMED, isInternalTransfer = internal)
    @Test fun reservesMonthlyTransportAndSavingsOnce() {
        val result = DailyAllowance.calculate(profile, listOf(expense(6000, 7), expense(20000, 8)),
            setOf(7), TransportPlan(6000, true), day, zone)
        assertEquals(22, result.transportDays)
        assertEquals(132000L, result.transportReserve)
        assertEquals(2368000L, result.monthlyBudget)
        assertEquals(20000L, result.todaySpent)
        assertEquals(197333L, result.dailyLimit)
    }
    @Test fun irregularTopupDoesNotConsumeAllowance() {
        val without = DailyAllowance.calculate(profile, emptyList(), setOf(7), TransportPlan(), day, zone)
        val with = DailyAllowance.calculate(profile, listOf(expense(100000, 9, type = TransactionType.INTERNAL_TRANSFER, internal = true),
            expense(6000, 7)), setOf(7), TransportPlan(), day, zone)
        assertEquals(without.dailyLimit, with.dailyLimit)
        assertEquals(0L, with.todaySpent)
    }
    @Test fun priorSpendingAndTodayAreNotSubtractedTwice() {
        val result = DailyAllowance.calculate(profile, listOf(expense(120000, 8, day.minusDays(1)), expense(10000, 8)),
            setOf(7), TransportPlan(0, false), day, zone)
        assertEquals(120000L, result.spentBeforeToday)
        assertEquals((2500000L - 120000L) / 12 - 10000L, result.remainingToday)
    }
    @Test fun transportOverspendIsReservedWithoutNegativeDailyLimit() {
        val result = DailyAllowance.calculate(profile, listOf(expense(3000000, 7)), setOf(7), TransportPlan(), day, zone)
        assertEquals(3000000L, result.transportReserve)
        assertEquals(0L, result.dailyLimit)
    }
}

package com.luxwallet.app.engine

import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class PaydayPlanTest {
    private val zone = ZoneId.of("Asia/Jakarta")
    private val start = LocalDate.of(2026, 9, 19)
    private fun time(day: LocalDate, hour: Int = 0) = day.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
    private fun plan() = PaydayPlan(time(start), start.toEpochDay(), start.withDayOfMonth(25).toEpochDay(), 3_200_000,
        business = 1_000_000, investment = 1_500_000)
    private fun tx(day: LocalDate, amount: Long, category: Long? = null, incoming: Boolean = false) = TransactionEntity(
        type = if (incoming) TransactionType.INCOME else TransactionType.EXPENSE,
        direction = if (incoming) TransactionDirection.IN else TransactionDirection.OUT,
        amount = amount, sourceAccountId = 1, categoryId = category, transactionTime = time(day, 12),
        createdAt = time(day, 12), updatedAt = time(day, 12), confidenceScore = 1.0, reviewStatus = ReviewStatus.CONFIRMED)
    @Test fun allocatesKnownCashOnlyThroughPaydayWithoutInventingIncome() {
        val p = plan().copy(expectedOn25 = 8_000_000, expectedOn1 = 9_000_000)
        assertEquals(6, p.days)
        assertEquals(24_000L, p.transportReserve)
        assertEquals(676_000L, p.freeCash)
        assertEquals(112_666L, p.dailyBudget)
        assertEquals(3_200_000L, PaydayMath.status(p, emptyList(), setOf(1), setOf(2), start, zone).cashRemaining)
    }
    @Test fun actualIncomeIsCountedOnceButDoesNotRewriteDailyTarget() {
        val p = plan()
        val s = PaydayMath.status(p, listOf(tx(start, 100_000, incoming = true)), emptySet(), emptySet(), start, zone)
        assertEquals(3_300_000L, s.cashRemaining)
        assertEquals(p.dailyBudget, s.dailyBudget)
    }
    @Test fun transportAndBillsConsumeTheirReserveBeforeFreeBudget() {
        val p = plan().copy(bills = 100_000)
        val day = start.plusDays(2)
        val s = PaydayMath.status(p, listOf(tx(day, 6_000, 1), tx(day, 100_000, 2), tx(day, 10_000)), setOf(1), setOf(2), day, zone)
        assertEquals(10_000L, s.spentToday)
        assertEquals(p.freeCash - 10_000, s.freeRemaining)
        val over = PaydayMath.status(p, listOf(tx(day, 110_000, 2)), setOf(1), setOf(2), day, zone)
        assertEquals(10_000L, over.spentToday)
    }
    @Test fun spendingBeforeSnapshotAndOwnTransfersNeverCountTwice() {
        val p = plan().copy(capturedAt = time(start, 15))
        val transfer = tx(start.plusDays(1), 200_000).copy(isInternalTransfer = true)
        val ignored = tx(start.plusDays(1), 500_000).copy(reviewStatus = ReviewStatus.IGNORED)
        val held = tx(start.plusDays(1), 3).copy(isExcludedFromCashflow = true, reviewStatus = ReviewStatus.NEEDS_REVIEW)
        val s = PaydayMath.status(p, listOf(tx(start, 100_000), transfer, ignored, held), emptySet(), emptySet(), start.plusDays(1), zone)
        assertEquals(p.availableCash, s.cashRemaining)
        assertEquals(0L, s.spentToday)
    }
    @Test fun historicalTargetsUseThePlanSavedForThatDay() {
        val old = plan()
        val new = plan().copy(startDay = start.plusDays(2).toEpochDay(), capturedAt = time(start.plusDays(2)), availableCash = 8_000_000)
        assertEquals(old, PaydayPlan.forDay(listOf(old, new), start.plusDays(1)))
        assertEquals(new, PaydayPlan.forDay(listOf(old, new), start.plusDays(2)))
        assertNull(PaydayPlan.forDay(listOf(old), start.minusDays(1)))
    }
    @Test fun shortageIsVisibleAndBudgetNeverPromisesUnavailableCash() {
        val p = plan().copy(availableCash = 500_000)
        val s = PaydayMath.status(p, emptyList(), emptySet(), emptySet(), start, zone)
        assertEquals(0L, s.dailyBudget)
        assertTrue(s.freeRemaining < 0)
        assertTrue(s.remainingToday < 0)
    }
    @Test fun nextPaydayHandlesMonthEndYearEndAndSameDay() {
        assertEquals(LocalDate.of(2026, 10, 1), PaydayPlan.nextPayday(LocalDate.of(2026, 9, 25)))
        assertEquals(LocalDate.of(2027, 1, 1), PaydayPlan.nextPayday(LocalDate.of(2026, 12, 31)))
        assertEquals(LocalDate.of(2026, 2, 25), PaydayPlan.nextPayday(LocalDate.of(2026, 2, 1)))
        assertEquals(LocalDate.of(2028, 3, 1), PaydayPlan.nextPayday(LocalDate.of(2028, 2, 29)))
    }
    @Test fun expiredPlanDoesNotAssumePaydayArrived() {
        val p = plan()
        val s = PaydayMath.status(p, listOf(tx(p.end, 10_000_000, incoming = true)), emptySet(), emptySet(), p.end, zone)
        assertTrue(s.expired)
        assertEquals(p.availableCash, s.cashRemaining)
    }
    @Test fun remindersRespectQuietHoursAndOnceADay() {
        assertFalse(MoneyCoach.canNotify(start, null, 8))
        assertFalse(MoneyCoach.canNotify(start, null, 21))
        assertFalse(MoneyCoach.canNotify(start, start.toString(), 12))
        assertTrue(MoneyCoach.canNotify(start, start.minusDays(1).toString(), 9))
        assertEquals("Mulai dari uang yang tersedia", MoneyCoach.advise(null, start).title)
    }
}

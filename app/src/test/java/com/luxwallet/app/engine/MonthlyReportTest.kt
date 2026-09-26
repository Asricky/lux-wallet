package com.luxwallet.app.engine

import com.luxwallet.app.core.database.entity.*
import com.luxwallet.app.core.model.*
import com.luxwallet.app.core.common.TransportPlan
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class MonthlyReportTest {
    private val zone=ZoneId.of("Asia/Jakarta")
    private val day=LocalDate.of(2026,9,26)
    private fun at(date:LocalDate)=date.atTime(12,0).atZone(zone).toInstant().toEpochMilli()
    private fun tx(amount:Long=10000, date:LocalDate=day, direction:TransactionDirection=TransactionDirection.OUT)=TransactionEntity(
        type=if(direction==TransactionDirection.IN)TransactionType.INCOME else TransactionType.EXPENSE,
        direction=direction,amount=amount,sourceAccountId=1,transactionTime=at(date),createdAt=at(date),updatedAt=at(date),
        confidenceScore=1.0,reviewStatus=ReviewStatus.CONFIRMED)
    private fun report(txs:List<TransactionEntity>,plans:List<PaydayPlan> = emptyList(),now:Long=at(day))=MonthlyReportEngine.create(
        YearMonth.from(day),txs,mapOf(1L to "Makan"),plans,emptySet(),emptySet(),now,zone)
    @Test fun totalsExcludeTransfersCorrectionsIgnoredTopupsAndFuture() {
        val r=report(listOf(tx(),tx(50000,direction=TransactionDirection.IN),tx(999).copy(isInternalTransfer=true),
            tx(999).copy(type=TransactionType.BALANCE_ADJUSTMENT),tx(999).copy(type=TransactionType.EWALLET_TOPUP),
            tx(999).copy(reviewStatus=ReviewStatus.IGNORED),tx(999,date=day.plusDays(1)),tx(999,date=day.minusMonths(1)),
            tx(999).copy(isExcludedFromCashflow=true)))
        assertEquals(50000L,r.income);assertEquals(10000L,r.expense);assertEquals(40000L,r.net)
        assertEquals(2,r.transactions.size);assertEquals(5,r.excluded);assertTrue(r.partial)
        assertEquals(26,r.days.size);assertEquals(0,r.coveredDays);assertEquals(0,r.overDays)
    }
    @Test fun midnightUsesLocalMonthAndLeapFebruaryEndsCorrectly() {
        val date=LocalDate.of(2024,2,29)
        val midnight=date.atStartOfDay(zone).toInstant().toEpochMilli()
        val records=listOf(tx(200,date).copy(transactionTime=midnight),tx(500,date).copy(transactionTime=date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()))
        val r=MonthlyReportEngine.create(YearMonth.of(2024,2),records,emptyMap(),emptyList(),emptySet(),emptySet(),at(date.plusDays(2)),zone)
        assertEquals(29,r.days.size);assertEquals(200L,r.expense);assertFalse(r.partial)
    }
    @Test fun budgetUsesSavedPlansAndDoesNotJudgeUnplannedDays() {
        val plan=PaydayPlan(day.atStartOfDay(zone).toInstant().toEpochMilli(),day.toEpochDay(),day.plusDays(1).toEpochDay(),5000,transportDaily=0)
        val r=report(listOf(tx(),tx(500000,day.minusDays(1))),listOf(plan))
        assertEquals(1,r.coveredDays);assertEquals(1,r.overDays);assertEquals(5000L,r.budgetExcess)
        assertTrue(r.verdict.contains("Lumi tegas"))
        val provisional=report(listOf(tx().copy(reviewStatus=ReviewStatus.NEEDS_REVIEW)),listOf(plan))
        assertEquals(1,provisional.pending);assertTrue(provisional.verdict.contains("sementara"))
    }
    @Test fun transportAndBillsUseReservesBeforeDailyBudgetJudgment() {
        val plan=PaydayPlan(day.atStartOfDay(zone).toInstant().toEpochMilli(),day.toEpochDay(),day.plusDays(1).toEpochDay(),160000,
            bills=50000,transportDaily=6000,weekdaysOnly=false)
        val r=MonthlyReportEngine.create(YearMonth.from(day),listOf(tx(104000),tx(6000).copy(categoryId=9),tx(50000).copy(categoryId=8)),
            emptyMap(),listOf(plan),setOf(9L),setOf(8L),at(day),zone)
        assertEquals(160000L,r.expense);assertEquals(104000L,r.days.last().budgetSpent!!)
        assertEquals(0,r.overDays);assertEquals(0L,r.budgetExcess)
    }
    @Test fun emptyReportDoesNotInventSuccessOrTreatZeroIncomeAsRatio() {
        assertTrue(report(emptyList()).verdict.contains("Belum ada"))
        assertTrue(report(listOf(tx())).verdict.contains("lebih besar"))
    }
    @Test(expected=IllegalArgumentException::class) fun futureMonthIsRejected() {
        MonthlyReportEngine.create(YearMonth.of(2027,1),emptyList(),emptyMap(),emptyList(),emptySet(),emptySet(),at(day),zone)
    }
    private fun interim(cash:Long?,txs:List<TransactionEntity> = emptyList(),plans:List<PaydayPlan> = emptyList(),profile:FinancialProfileEntity=FinancialProfileEntity(baselineDate=0))=
        InterimBudgetEngine.calculate(plans,cash,txs,setOf(9L),setOf(8L),profile,TransportPlan(6000,true),day,zone)
    @Test fun interimUsesActualCashProtectsReservesAndExcludesTransport() {
        val old=PaydayPlan(at(day.minusDays(7)),day.minusDays(7).toEpochDay(),day.minusDays(1).toEpochDay(),9999999,
            business=1000000,investment=1500000,transportDaily=6000,expectedOn1=9000000)
        val result=interim(3200000,listOf(tx(20000),tx(6000).copy(categoryId=9),tx(100000).copy(isInternalTransfer=true)),listOf(old))!!
        assertEquals(5,result.days);assertEquals(LocalDate.of(2026,10,1),result.until)
        assertEquals(3200000L,result.cash);assertEquals(2512000L,result.reserved)
        assertEquals(20000L,result.spent);assertEquals(141600L,result.daily);assertEquals(121600L,result.remaining)
    }
    @Test fun interimIsNeverCreatedWithoutCashOrOverwritesActivePlan() {
        assertNull(interim(null))
        val active=PaydayPlan(at(day),day.toEpochDay(),day.plusDays(5).toEpochDay(),100000,transportDaily=0)
        assertNull(interim(100000,plans=listOf(active)))
        assertEquals(0L,interim(-1000)!!.remaining)
        assertEquals(0L,interim(1000,profile=FinancialProfileEntity(baselineDate=0,savingsTargetMonthly=1000000))!!.remaining)
    }
}

package com.luxwallet.app.engine

import com.luxwallet.app.core.common.CashflowMath
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.*
import java.time.*

data class ReportDay(val date: LocalDate, val income: Long, val expense: Long, val budget: Long?, val budgetSpent: Long?)
data class MonthlyReport(val month: YearMonth, val through: LocalDate, val generatedAt: Long,
    val transactions: List<TransactionEntity>, val categories: Map<Long, String>, val days: List<ReportDay>,
    val pending: Int, val excluded: Int) {
    val income = CashflowMath.totalIncome(transactions)
    val expense = CashflowMath.totalExpense(transactions)
    val net = income - expense
    val expenses = CashflowMath.expenseByCategory(transactions) { categories[it] ?: "Tanpa kategori" }
    val incomes = CashflowMath.incomeByCategory(transactions) { categories[it] ?: "Tanpa kategori" }
    val coveredDays = days.count { it.budget != null }
    val overDays = days.count { it.budget != null && it.budgetSpent!! > it.budget }
    val budgetExcess = days.sumOf { if (it.budget == null) 0L else (it.budgetSpent!! - it.budget).coerceAtLeast(0) }
    val partial = through < month.atEndOfMonth()
    val verdict get() = when {
        transactions.isEmpty() -> "Belum ada arus kas tercatat. Jangan menyimpulkan bulan ini hemat sebelum melengkapi catatan."
        pending > 0 -> "Cek catatan yang belum ditinjau dahulu. Angka dan evaluasi laporan ini masih sementara."
        overDays > 0 -> "Lumi tegas: batas belanja terlewati. Hentikan dulu belanja yang bisa ditunda, cek pemicunya, dan susun batas realistis. Jangan mengambil dana kebutuhan wajib untuk menutup keinginan sesaat."
        net < 0 -> "Pengeluaran tercatat lebih besar dari pemasukan. Cek apakah ada pemasukan yang belum dicatat atau penggunaan tabungan yang memang direncanakan. Tahan belanja tambahan sampai jelas."
        coveredDays == 0 -> "Arus kas sudah terlihat, tetapi belum ada budget harian tersimpan untuk menilai disiplin belanja. Buat rencana sebelum menyebut pengeluaran aman."
        else -> "Bagus, belanja pada hari yang memiliki rencana masih dalam batas. Pertahankan kebiasaan mencatat dan sisihkan dana tujuan setelah kebutuhan wajib terpenuhi."
    }
    val recommendations get() = buildList {
        if (pending > 0) add("Selesaikan tinjauan transaksi sebelum memakai laporan sebagai dasar keputusan.")
        add("Awali bulan depan dengan saldo nyata, jadwal pemasukan, tagihan, dan dana penyangga. Jangan masukkan gaji yang belum diterima sebagai saldo.")
        if (overDays > 0) add("Tinjau hari yang melewati budget. Beri jeda 24 jam sebelum belanja nonwajib dan periksa batas harian sebelum membayar.")
        if (expenses.isNotEmpty()) add("Periksa kategori pengeluaran terbesar di rincian laporan. Pisahkan kebutuhan wajib dari belanja yang bisa ditunda; porsi besar sendiri belum berarti boros.")
        add("Pisahkan cadangan transportasi, tabungan bisnis, dan investasi. Setorkan target hanya jika dana kebutuhan sampai pemasukan berikutnya sudah cukup.")
        if (coveredDays < days.size) add("Lengkapi rencana harian. Hari tanpa rencana tidak dinilai surplus atau melampaui budget.")
    }
}

object MonthlyReportEngine {
    fun create(month: YearMonth, transactions: List<TransactionEntity>, categories: Map<Long, String>, plans: List<PaydayPlan>,
        transportIds: Set<Long>, billIds: Set<Long>, now: Long = System.currentTimeMillis(), zone: ZoneId = ZoneId.systemDefault()): MonthlyReport {
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        require(month <= YearMonth.from(today)) { "Laporan bulan mendatang belum tersedia." }
        val through = minOf(today, month.atEndOfMonth())
        val monthly = transactions.filter {
            val date = Instant.ofEpochMilli(it.transactionTime).atZone(zone).toLocalDate()
            YearMonth.from(date) == month && it.transactionTime <= now
        }
        val eligible = CashflowMath.cashflowEligible(monthly).filter {
            it.type !in setOf(TransactionType.BALANCE_ADJUSTMENT, TransactionType.EWALLET_TOPUP)
        }.sortedBy { it.transactionTime }
        val grouped = eligible.groupBy { Instant.ofEpochMilli(it.transactionTime).atZone(zone).toLocalDate() }
        val days = (1..through.dayOfMonth).map { day ->
            val date = month.atDay(day)
            val records = grouped[date].orEmpty()
            val status = PaydayPlan.forDay(plans, date)?.let { PaydayMath.status(it, transactions.filter { tx -> tx.transactionTime <= now }, transportIds, billIds, date, zone) }
            ReportDay(date, CashflowMath.totalIncome(records), CashflowMath.totalExpense(records), status?.dailyBudget, status?.spentToday)
        }
        return MonthlyReport(month, through, now, eligible, categories, days,
            monthly.count { it.reviewStatus == ReviewStatus.NEEDS_REVIEW }, monthly.size - eligible.size)
    }
}

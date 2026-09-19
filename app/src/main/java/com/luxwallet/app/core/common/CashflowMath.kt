package com.luxwallet.app.core.common

import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.ReviewStatus
import com.luxwallet.app.core.model.TransactionDirection

/**
 * Shared cashflow aggregation used by Home and Cashflow (PRD §26/§27). Internal transfers are
 * always excluded from income/expense totals (PRD §11) — this is the one place that filter lives
 * so both screens stay consistent.
 */
object CashflowMath {
    fun cashflowEligible(transactions: List<TransactionEntity>): List<TransactionEntity> =
        transactions.filter {
            !it.isInternalTransfer && !it.isExcludedFromCashflow && it.reviewStatus != ReviewStatus.IGNORED
        }

    fun totalIncome(transactions: List<TransactionEntity>): Long =
        cashflowEligible(transactions).filter { it.direction == TransactionDirection.IN }.sumOf { it.amount }

    fun totalExpense(transactions: List<TransactionEntity>): Long =
        cashflowEligible(transactions).filter { it.direction == TransactionDirection.OUT }.sumOf { it.amount }

    /** Groups expense transactions by resolved category display name -> total amount, descending. */
    fun expenseByCategory(transactions: List<TransactionEntity>, categoryName: (Long?) -> String): List<Pair<String, Long>> =
        cashflowEligible(transactions)
            .filter { it.direction == TransactionDirection.OUT }
            .groupBy { categoryName(it.categoryId) }
            .map { (name, txs) -> name to txs.sumOf { it.amount } }
            .sortedByDescending { it.second }

    fun incomeByCategory(transactions: List<TransactionEntity>, categoryName: (Long?) -> String): List<Pair<String, Long>> =
        cashflowEligible(transactions)
            .filter { it.direction == TransactionDirection.IN }
            .groupBy { categoryName(it.categoryId) }
            .map { (name, txs) -> name to txs.sumOf { it.amount } }
            .sortedByDescending { it.second }
}

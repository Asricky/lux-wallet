package com.luxwallet.app.engine

data class NetWorthInput(
    val accountBalances: List<Long>, // accounts with includeInNetWorth = true
    val otherAssetValues: List<Long>, // assets with includeInNetWorth = true
    val liabilityOutstanding: List<Long>
)

data class NetWorthResult(
    val totalAssets: Long,
    val totalLiabilities: Long,
    val netWorth: Long
)

/** Net worth = Total Assets - Total Liabilities (PRD §15). Cashflow (income/expense) is tracked separately. */
object NetWorthEngine {
    fun calculate(input: NetWorthInput): NetWorthResult {
        val totalAssets = input.accountBalances.sum() + input.otherAssetValues.sum()
        val totalLiabilities = input.liabilityOutstanding.sum()
        return NetWorthResult(
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            netWorth = totalAssets - totalLiabilities
        )
    }
}

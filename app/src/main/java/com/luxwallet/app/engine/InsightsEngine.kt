package com.luxwallet.app.engine

data class CategorySpend(val categoryName: String, val currentMonthAmount: Long, val trailingAverageAmount: Long)

data class Insight(val message: String, val severity: InsightSeverity)

enum class InsightSeverity { INFO, POSITIVE, WARNING }

data class InsightsInput(
    val categorySpends: List<CategorySpend>,
    val discretionaryBudget: Long,
    val discretionarySpent: Long,
    val cashAllocationPercent: Double?,
    val cashAllocationTargetRange: ClosedFloatingPointRange<Double>?
)

/**
 * Deterministic, rule-based financial insights (PRD §35). Every message is derived from
 * a concrete number comparison so it can always be traced back to the underlying data.
 */
object InsightsEngine {
    private const val SIGNIFICANT_CHANGE_PERCENT = 15.0

    fun generate(input: InsightsInput): List<Insight> {
        val insights = mutableListOf<Insight>()

        for (spend in input.categorySpends) {
            if (spend.trailingAverageAmount <= 0) continue
            val changePercent = ((spend.currentMonthAmount - spend.trailingAverageAmount).toDouble() /
                spend.trailingAverageAmount) * 100.0

            if (changePercent >= SIGNIFICANT_CHANGE_PERCENT) {
                insights += Insight(
                    "${spend.categoryName} spending is ${changePercent.roundToInt()}% higher than your 3-month average.",
                    InsightSeverity.WARNING
                )
            } else if (changePercent <= -SIGNIFICANT_CHANGE_PERCENT) {
                val diff = spend.trailingAverageAmount - spend.currentMonthAmount
                insights += Insight(
                    "${spend.categoryName} spending is down Rp${diff.formatThousands()} versus your average.",
                    InsightSeverity.POSITIVE
                )
            }
        }

        if (input.discretionaryBudget > 0) {
            val usedPercent = (input.discretionarySpent.toDouble() / input.discretionaryBudget) * 100.0
            insights += Insight(
                "You have used ${usedPercent.roundToInt()}% of your monthly discretionary budget.",
                when {
                    usedPercent >= 100.0 -> InsightSeverity.WARNING
                    usedPercent >= 80.0 -> InsightSeverity.WARNING
                    else -> InsightSeverity.INFO
                }
            )
        }

        val cashPercent = input.cashAllocationPercent
        val cashRange = input.cashAllocationTargetRange
        if (cashPercent != null && cashRange != null && cashPercent > cashRange.endInclusive) {
            insights += Insight("Your cash allocation is above your selected target range.", InsightSeverity.WARNING)
        }

        return insights
    }

    private fun Double.roundToInt(): Int = Math.round(this).toInt()
    private fun Long.formatThousands(): String = "%,d".format(this).replace(',', '.')
}

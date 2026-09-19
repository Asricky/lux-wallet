package com.luxwallet.app.engine

import com.luxwallet.app.core.model.RiskProfile

data class InvestmentGuidanceInput(
    val monthlyIncome: Long,
    val monthlyExpenses: Long,
    val monthlyDebtPayments: Long,
    val liquidAssets: Long,
    val emergencyFundMonthsTarget: Double,
    val riskProfile: RiskProfile,
    /** Months until the user's nearest, highest-priority goal target date; null if no goals set. */
    val nearestGoalHorizonMonths: Int? = null
)

data class GuidanceItem(val category: String, val rationale: List<String>)

data class InvestmentGuidanceResult(
    val priorityStatement: String,
    val emergencyFundMonthsCurrent: Double,
    val items: List<GuidanceItem>
)

/**
 * Deterministic, rule-based investment guidance (PRD §33). Informational only — never executes
 * trades, never guarantees returns, never recommends individual stocks/tokens in MVP. Emergency
 * fund coverage and debt burden always take priority over risk-profile-driven allocation
 * (PRD §34: risk profile "must not override emergency fund or debt considerations").
 */
object InvestmentGuidanceEngine {
    private const val HIGH_DEBT_BURDEN_RATIO = 0.30
    private const val SHORT_GOAL_HORIZON_MONTHS = 12

    fun assess(input: InvestmentGuidanceInput): InvestmentGuidanceResult {
        val emergencyMonths = if (input.monthlyExpenses > 0) {
            input.liquidAssets.toDouble() / input.monthlyExpenses
        } else 0.0

        val debtBurdenRatio = if (input.monthlyIncome > 0) {
            input.monthlyDebtPayments.toDouble() / input.monthlyIncome
        } else 0.0

        val items = mutableListOf<GuidanceItem>()
        val priority: String

        when {
            emergencyMonths < input.emergencyFundMonthsTarget -> {
                priority = "Build emergency reserves before increasing long-term risk exposure."
                items += GuidanceItem(
                    category = "Emergency liquidity",
                    rationale = listOf(
                        "Emergency fund is currently %.1f months".format(emergencyMonths),
                        "Target is %.0f months".format(input.emergencyFundMonthsTarget),
                        "High-liquidity allocation is still below target"
                    )
                )
                items += GuidanceItem(
                    category = "Cash equivalent",
                    rationale = listOf("Keep building liquid savings until the emergency fund target is met")
                )
            }

            debtBurdenRatio > HIGH_DEBT_BURDEN_RATIO -> {
                priority = "Prioritize paying down debt before increasing investment contributions."
                items += GuidanceItem(
                    category = "Debt paydown",
                    rationale = listOf(
                        "Monthly debt payments are %.0f%% of monthly income".format(debtBurdenRatio * 100),
                        "A ratio above %.0f%% is generally considered a high debt burden".format(HIGH_DEBT_BURDEN_RATIO * 100)
                    )
                )
            }

            else -> {
                priority = "Emergency fund and debt are in a healthy range; continue diversifying based on your risk profile."
                items += riskBasedAllocation(input.riskProfile)
                input.nearestGoalHorizonMonths?.let { horizon ->
                    if (horizon < SHORT_GOAL_HORIZON_MONTHS) {
                        items += GuidanceItem(
                            category = "Cash equivalent",
                            rationale = listOf(
                                "Your nearest goal is $horizon months away",
                                "Short-horizon goals should stay in low-volatility instruments regardless of overall risk profile"
                            )
                        )
                    }
                }
            }
        }

        return InvestmentGuidanceResult(priority, emergencyMonths, items)
    }

    private fun riskBasedAllocation(riskProfile: RiskProfile): List<GuidanceItem> = when (riskProfile) {
        RiskProfile.CONSERVATIVE -> listOf(
            GuidanceItem(
                "Deposito",
                listOf("Conservative risk profile favors capital preservation", "Deposito offers predictable, low-volatility returns")
            ),
            GuidanceItem(
                "Government bonds / SBN",
                listOf("Government-backed instruments diversify beyond cash with modest additional yield")
            )
        )
        RiskProfile.MODERATE -> listOf(
            GuidanceItem(
                "Bond fund",
                listOf("Moderate risk profile supports a mix of bonds and equities", "Bond funds add diversification and moderate volatility")
            ),
            GuidanceItem(
                "Broad equity exposure",
                listOf("Broad, diversified equity exposure captures long-term growth without single-stock risk")
            )
        )
        RiskProfile.GROWTH -> listOf(
            GuidanceItem(
                "Broad equity exposure",
                listOf(
                    "Growth risk profile can tolerate more volatility for higher long-term expected return",
                    "Broad exposure avoids concentration in individual stocks"
                )
            ),
            GuidanceItem("Gold", listOf("Gold can help diversify against equity and currency volatility"))
        )
    }
}

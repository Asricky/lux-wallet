package com.luxwallet.app.engine

import com.luxwallet.app.core.model.RiskProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InvestmentGuidanceEngineTest {

    @Test
    fun belowEmergencyFundTarget_prioritizesLiquidity() {
        val result = InvestmentGuidanceEngine.assess(
            InvestmentGuidanceInput(
                monthlyIncome = 15_000_000,
                monthlyExpenses = 8_000_000,
                monthlyDebtPayments = 0,
                liquidAssets = 18_400_000, // 2.3 months of expenses
                emergencyFundMonthsTarget = 6.0,
                riskProfile = RiskProfile.GROWTH
            )
        )
        assertEquals("Build emergency reserves before increasing long-term risk exposure.", result.priorityStatement)
        assertTrue(result.items.any { it.category == "Emergency liquidity" })
        assertEquals(2.3, result.emergencyFundMonthsCurrent, 0.01)
    }

    @Test
    fun highDebtBurden_prioritizesDebtPaydownEvenWithEnoughEmergencyFund() {
        val result = InvestmentGuidanceEngine.assess(
            InvestmentGuidanceInput(
                monthlyIncome = 10_000_000,
                monthlyExpenses = 4_000_000,
                monthlyDebtPayments = 3_500_000, // 35% of income
                liquidAssets = 30_000_000, // well above emergency target
                emergencyFundMonthsTarget = 6.0,
                riskProfile = RiskProfile.CONSERVATIVE
            )
        )
        assertEquals("Prioritize paying down debt before increasing investment contributions.", result.priorityStatement)
        assertTrue(result.items.any { it.category == "Debt paydown" })
    }

    @Test
    fun healthyFinances_usesRiskProfileAllocation() {
        val result = InvestmentGuidanceEngine.assess(
            InvestmentGuidanceInput(
                monthlyIncome = 10_000_000,
                monthlyExpenses = 4_000_000,
                monthlyDebtPayments = 0,
                liquidAssets = 30_000_000,
                emergencyFundMonthsTarget = 6.0,
                riskProfile = RiskProfile.CONSERVATIVE
            )
        )
        assertTrue(result.items.any { it.category == "Deposito" })
        // Every guidance item must include rationale (PRD §33 "clear Why explanation").
        assertTrue(result.items.all { it.rationale.isNotEmpty() })
    }

    @Test
    fun shortGoalHorizon_addsCashEquivalentEvenForGrowthProfile() {
        val result = InvestmentGuidanceEngine.assess(
            InvestmentGuidanceInput(
                monthlyIncome = 10_000_000,
                monthlyExpenses = 4_000_000,
                monthlyDebtPayments = 0,
                liquidAssets = 30_000_000,
                emergencyFundMonthsTarget = 6.0,
                riskProfile = RiskProfile.GROWTH,
                nearestGoalHorizonMonths = 6
            )
        )
        assertTrue(result.items.any { it.category == "Cash equivalent" })
    }
}

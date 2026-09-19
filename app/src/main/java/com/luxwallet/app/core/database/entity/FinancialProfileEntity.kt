package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luxwallet.app.core.model.RiskProfile
import kotlinx.serialization.Serializable

/**
 * Single-row table (id is always [SINGLETON_ID]) holding the onboarding financial profile that
 * drives Safe-to-Spend and investment guidance (PRD §13, §30, §33). Editable later in Settings.
 */
@Serializable
@Entity(tableName = "financial_profile")
data class FinancialProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val baselineDate: Long,
    val expectedMonthlyIncome: Long = 0,
    val fixedObligations: Long = 0,
    val debtPayments: Long = 0,
    val savingsTargetMonthly: Long = 0,
    val investmentTargetMonthly: Long = 0,
    val plannedExpensesMonthly: Long = 0,
    val safetyBufferMonthly: Long = 0,
    val emergencyFundMonthsTarget: Double = 6.0,
    val riskProfile: RiskProfile = RiskProfile.MODERATE,
    val onboardingCompleted: Boolean = false
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}

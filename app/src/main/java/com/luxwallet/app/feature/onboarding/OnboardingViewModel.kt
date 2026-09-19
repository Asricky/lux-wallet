package com.luxwallet.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.AppPreferences
import com.luxwallet.app.core.database.entity.AssetEntity
import com.luxwallet.app.core.database.entity.FinancialProfileEntity
import com.luxwallet.app.core.database.entity.GoalEntity
import com.luxwallet.app.core.database.entity.LiabilityEntity
import com.luxwallet.app.core.model.AccountKind
import com.luxwallet.app.core.model.AccountProvider
import com.luxwallet.app.core.model.AssetClass
import com.luxwallet.app.core.model.LiabilityType
import com.luxwallet.app.core.model.RiskProfile
import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.data.AccountRepository
import com.luxwallet.app.data.AssetRepository
import com.luxwallet.app.data.FinancialProfileRepository
import com.luxwallet.app.data.GoalRepository
import com.luxwallet.app.data.LiabilityRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val preferences: AppPreferences,
    private val accountRepository: AccountRepository,
    private val assetRepository: AssetRepository,
    private val liabilityRepository: LiabilityRepository,
    private val financialProfileRepository: FinancialProfileRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    fun setSourceEnabled(source: SourceApp, enabled: Boolean) {
        viewModelScope.launch { preferences.setSourceEnabled(source, enabled) }
    }

    fun createAccountIfNamed(name: String, provider: AccountProvider, kind: AccountKind, openingBalance: Long) {
        if (name.isBlank()) return
        viewModelScope.launch {
            accountRepository.createAccount(name, kind, provider, openingBalance, System.currentTimeMillis())
        }
    }

    fun addAssetIfNamed(name: String, assetClass: AssetClass, value: Long) {
        if (name.isBlank() || value <= 0) return
        viewModelScope.launch {
            assetRepository.upsert(AssetEntity(name = name, assetClass = assetClass, currentValue = value, updatedAt = System.currentTimeMillis()))
        }
    }

    fun addLiabilityIfNamed(name: String, type: LiabilityType, outstanding: Long) {
        if (name.isBlank() || outstanding <= 0) return
        viewModelScope.launch {
            liabilityRepository.upsert(LiabilityEntity(name = name, type = type, currentOutstanding = outstanding, updatedAt = System.currentTimeMillis()))
        }
    }

    fun addGoalIfNamed(name: String, targetAmount: Long) {
        if (name.isBlank() || targetAmount <= 0) return
        viewModelScope.launch {
            goalRepository.upsert(GoalEntity(name = name, targetAmount = targetAmount, createdAt = System.currentTimeMillis()))
        }
    }

    fun finish(
        expectedMonthlyIncome: Long,
        fixedObligations: Long,
        savingsTarget: Long,
        investmentTarget: Long,
        safetyBuffer: Long,
        riskProfile: RiskProfile
    ) {
        viewModelScope.launch {
            financialProfileRepository.save(
                FinancialProfileEntity(
                    baselineDate = System.currentTimeMillis(),
                    expectedMonthlyIncome = expectedMonthlyIncome,
                    fixedObligations = fixedObligations,
                    savingsTargetMonthly = savingsTarget,
                    investmentTargetMonthly = investmentTarget,
                    safetyBufferMonthly = safetyBuffer,
                    riskProfile = riskProfile,
                    onboardingCompleted = true
                )
            )
            preferences.setOnboardingComplete(true)
        }
    }

    companion object {
        fun create(app: LuxWalletApp) = OnboardingViewModel(
            app.preferences, app.accountRepository, app.assetRepository,
            app.liabilityRepository, app.financialProfileRepository, app.goalRepository
        )
    }
}

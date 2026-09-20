package com.luxwallet.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.FinancialProfileEntity
import com.luxwallet.app.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(private val app: LuxWalletApp) : ViewModel() {
    val saving = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    fun finish(balances: Map<AccountProvider, Long>, sources: Set<SourceApp>, income: Long, obligations: Long, savings: Long, investment: Long, profileName: String = "") {
        if (saving.value) return
        saving.value = true
        viewModelScope.launch {
            try {
                app.database.withTransaction {
                    val existing = app.database.accountDao().getAllAccountsOnce()
                    balances.forEach { (provider, balance) ->
                        if (existing.none { it.provider == provider && it.isOwnedByUser }) {
                            val kind = when (provider) {
                                AccountProvider.BCA, AccountProvider.SEABANK -> AccountKind.BANK
                                AccountProvider.GOPAY, AccountProvider.SHOPEEPAY -> AccountKind.EWALLET
                                else -> AccountKind.MANUAL
                            }
                            val name = when (provider) { AccountProvider.CASH -> "Tunai"; AccountProvider.SEABANK -> "SeaBank"; AccountProvider.GOPAY -> "GoPay"; AccountProvider.SHOPEEPAY -> "ShopeePay"; else -> provider.name }
                            app.accountRepository.createAccount(name, kind, provider, balance, System.currentTimeMillis())
                        }
                    }
                    app.financialProfileRepository.save(FinancialProfileEntity(baselineDate = System.currentTimeMillis(),
                        expectedMonthlyIncome = income, fixedObligations = obligations, savingsTargetMonthly = savings,
                        investmentTargetMonthly = investment, onboardingCompleted = true))
                }
                SourceApp.entries.forEach { app.preferences.setSourceEnabled(it, it in sources) }
                app.preferences.setProfileName(profileName)
                app.preferences.setOnboardingComplete(true)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error.value = "Pengaturan belum selesai disimpan. Coba lagi."
            } finally { saving.value = false }
        }
    }
    companion object { fun create(app: LuxWalletApp) = OnboardingViewModel(app) }
}

package com.luxwallet.app.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.AccountEntity
import com.luxwallet.app.core.model.AccountKind
import com.luxwallet.app.core.model.AccountProvider
import com.luxwallet.app.data.AccountRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(private val accountRepository: AccountRepository) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.observeAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createAccount(name: String, kind: AccountKind, provider: AccountProvider, openingBalance: Long) {
        viewModelScope.launch {
            accountRepository.createAccount(
                name = name, kind = kind, provider = provider,
                openingBalance = openingBalance, openingBalanceDate = System.currentTimeMillis()
            )
        }
    }

    fun reconcile(account: AccountEntity, newBalance: Long) {
        viewModelScope.launch {
            accountRepository.reconcile(account.id, newBalance, System.currentTimeMillis())
        }
    }

    fun setIncludeInNetWorth(account: AccountEntity, include: Boolean) {
        viewModelScope.launch { accountRepository.update(account.copy(includeInNetWorth = include)) }
    }

    fun setActive(account: AccountEntity, active: Boolean) {
        viewModelScope.launch { accountRepository.update(account.copy(isActive = active)) }
    }

    companion object {
        fun create(app: LuxWalletApp) = AccountsViewModel(app.accountRepository)
    }
}

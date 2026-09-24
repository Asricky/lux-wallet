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

    val saving = kotlinx.coroutines.flow.MutableStateFlow(false)
    val error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    fun createAccount(name: String, kind: AccountKind, provider: AccountProvider, openingBalance: Long, onSaved: () -> Unit = {}) {
        if (saving.value) return
        if (name.isBlank() || openingBalance < 0) { error.value = "Isi nama dan saldo nol atau positif."; return }
        saving.value = true
        error.value = null
        viewModelScope.launch {
          try {
            accountRepository.createAccount(
                name = name, kind = kind, provider = provider,
                openingBalance = openingBalance, openingBalanceDate = System.currentTimeMillis()
            )
            onSaved()
          } catch (e: Exception) {
              if (e is kotlinx.coroutines.CancellationException) throw e
              error.value = "Rekening belum tersimpan. Coba lagi."
          } finally { saving.value = false }
        }
    }

    fun reconcile(account: AccountEntity, newBalance: Long) {
        viewModelScope.launch {
            accountRepository.reconcile(account.id, newBalance, System.currentTimeMillis())
        }
    }

    fun setIncludeInNetWorth(account: AccountEntity, include: Boolean) {
        change { accountRepository.setIncludeInNetWorth(account.id, include) }
    }

    fun setActive(account: AccountEntity, active: Boolean) {
        change { accountRepository.setActive(account.id, active) }
    }

    private fun change(action: suspend () -> Unit) {
        if (saving.value) return
        saving.value = true
        error.value = null
        viewModelScope.launch {
            try { action() }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { error.value = "Perubahan rekening belum tersimpan. Coba lagi." }
            finally { saving.value = false }
        }
    }

    companion object {
        fun create(app: LuxWalletApp) = AccountsViewModel(app.accountRepository)
    }
}

package com.luxwallet.app.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.CategoryEntity
import com.luxwallet.app.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TransactionDetailUiState(val transaction: TransactionEntity? = null, val mainCategories: List<CategoryEntity> = emptyList(), val loaded: Boolean = false, val accounts: List<com.luxwallet.app.core.database.entity.AccountEntity> = emptyList())
class TransactionDetailViewModel(private val app: LuxWalletApp, private val id: Long) : ViewModel() {
    val uiState = combine(app.transactionRepository.observeAll(), app.categoryRepository.observeMainCategories(), app.accountRepository.observeActiveAccounts()) { txs, cats, accounts ->
        TransactionDetailUiState(txs.firstOrNull { it.id == id }, cats, true, accounts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionDetailUiState())
    val saving = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    private fun perform(onDone: () -> Unit, action: suspend () -> Unit) {
        if (saving.value) return
        saving.value = true; error.value = null
        viewModelScope.launch {
            try { action(); onDone() }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { error.value = e.message ?: "Belum tersimpan. Coba lagi." }
            finally { saving.value = false }
        }
    }
    fun saveAndConfirm(merchant: String, note: String, categoryId: Long?, excluded: Boolean, learn: Boolean, accountId: Long?, onDone: () -> Unit) =
        perform(onDone) { app.transactionRepository.saveDetails(id, merchant, note, categoryId, excluded, learn, accountId) }
    fun markIgnored(onDone: () -> Unit) = perform(onDone) { app.transactionRepository.markIgnored(id) }
    companion object { fun create(app: LuxWalletApp, transactionId: Long) = TransactionDetailViewModel(app, transactionId) }
}

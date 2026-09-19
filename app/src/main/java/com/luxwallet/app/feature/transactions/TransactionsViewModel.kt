package com.luxwallet.app.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.data.AccountRepository
import com.luxwallet.app.data.CategoryRepository
import com.luxwallet.app.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionsUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val accountNames: Map<Long, String> = emptyMap(),
    val categoryNames: Map<Long, String> = emptyMap(),
    val query: String = "",
    val isLoading: Boolean = true
)

class TransactionsViewModel(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.observeAll(),
                accountRepository.observeAllAccounts(),
                categoryRepository.observeAll(),
                _query
            ) { transactions, accounts, categories, query ->
                val accountNames = accounts.associate { it.id to it.name }
                val categoryNames = categories.associate { it.id to it.name }
                val filtered = if (query.isBlank()) {
                    transactions
                } else {
                    transactions.filter {
                        (it.merchantName?.contains(query, ignoreCase = true) == true) ||
                            (it.counterpartyName?.contains(query, ignoreCase = true) == true) ||
                            (categoryNames[it.categoryId]?.contains(query, ignoreCase = true) == true)
                    }
                }
                TransactionsUiState(filtered, accountNames, categoryNames, query, isLoading = false)
            }.collect { _uiState.value = it }
        }
    }

    fun setQuery(query: String) = _query.update { query }

    companion object {
        fun create(app: LuxWalletApp) = TransactionsViewModel(app.accountRepository, app.categoryRepository, app.transactionRepository)
    }
}

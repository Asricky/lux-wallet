package com.luxwallet.app.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.ReviewStatus
import com.luxwallet.app.data.AccountRepository
import com.luxwallet.app.data.CategoryRepository
import com.luxwallet.app.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class NeedsReviewUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val accountNames: Map<Long, String> = emptyMap(),
    val categoryNames: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = true
)

class NeedsReviewViewModel(
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NeedsReviewUiState())
    val uiState: StateFlow<NeedsReviewUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.observeByReviewStatus(ReviewStatus.NEEDS_REVIEW),
                accountRepository.observeAllAccounts(),
                categoryRepository.observeAll()
            ) { transactions, accounts, categories ->
                NeedsReviewUiState(
                    transactions = transactions,
                    accountNames = accounts.associate { it.id to it.name },
                    categoryNames = categories.associate { it.id to it.name },
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    companion object {
        fun create(app: LuxWalletApp) = NeedsReviewViewModel(app.accountRepository, app.categoryRepository, app.transactionRepository)
    }
}

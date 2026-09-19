package com.luxwallet.app.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.CategoryEntity
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.ReviewStatus
import com.luxwallet.app.data.CategoryRepository
import com.luxwallet.app.data.MerchantRuleRepository
import com.luxwallet.app.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class TransactionDetailUiState(
    val transaction: TransactionEntity? = null,
    val mainCategories: List<CategoryEntity> = emptyList()
)

/**
 * Lets the user correct a parsed transaction (PRD §3.4, §24): category, merchant name, confirm,
 * exclude from analytics. Amount/direction/account are evidence from the notification and are
 * intentionally not editable here — a wrong amount should be fixed via a manual Balance
 * Adjustment instead, so the ledger never silently diverges from what was actually observed.
 */
class TransactionDetailViewModel(
    private val transactionId: Long,
    private val categoryRepository: CategoryRepository,
    private val merchantRuleRepository: MerchantRuleRepository,
    private val transactionRepositoryRef: TransactionRepository,
    private val rawTransactionDao: com.luxwallet.app.core.database.dao.TransactionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(transactionRepositoryRef.observeAll(), categoryRepository.observeMainCategories()) { all, cats ->
                TransactionDetailUiState(all.firstOrNull { it.id == transactionId }, cats)
            }.collect { _uiState.value = it }
        }
    }

    fun updateCategory(categoryId: Long, learnRuleForMerchant: Boolean) {
        val tx = _uiState.value.transaction ?: return
        viewModelScope.launch {
            rawTransactionDao.update(tx.copy(categoryId = categoryId, subcategoryId = null, updatedAt = System.currentTimeMillis()))
            val merchant = tx.merchantName
            if (learnRuleForMerchant && !merchant.isNullOrBlank()) {
                merchantRuleRepository.learnRule(merchant, categoryId, null)
            }
        }
    }

    fun updateMerchantName(name: String) {
        val tx = _uiState.value.transaction ?: return
        viewModelScope.launch {
            rawTransactionDao.update(tx.copy(merchantName = name.ifBlank { null }, updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateNote(note: String) {
        val tx = _uiState.value.transaction ?: return
        viewModelScope.launch {
            rawTransactionDao.update(tx.copy(note = note.ifBlank { null }, updatedAt = System.currentTimeMillis()))
        }
    }

    fun confirm() {
        val tx = _uiState.value.transaction ?: return
        viewModelScope.launch {
            transactionRepositoryRef.confirm(tx.id)
        }
    }

    fun saveAndConfirm(merchant: String, note: String) {
        viewModelScope.launch {
            val tx = rawTransactionDao.getById(transactionId) ?: return@launch
            if (tx.reviewStatus == ReviewStatus.IGNORED) return@launch
            transactionRepositoryRef.confirm(tx.id, merchant, note)
        }
    }

    fun setExcludedFromCashflow(excluded: Boolean) {
        val tx = _uiState.value.transaction ?: return
        if (tx.reviewReason == com.luxwallet.app.core.model.ReviewReason.POSSIBLE_DUPLICATE) return
        viewModelScope.launch {
            rawTransactionDao.update(tx.copy(isExcludedFromCashflow = excluded, updatedAt = System.currentTimeMillis()))
        }
    }

    fun markIgnored() {
        val tx = _uiState.value.transaction ?: return
        viewModelScope.launch {
            transactionRepositoryRef.markIgnored(tx.id)
        }
    }

    companion object {
        fun create(app: LuxWalletApp, transactionId: Long) = TransactionDetailViewModel(
            transactionId, app.categoryRepository, app.merchantRuleRepository, app.transactionRepository, app.database.transactionDao()
        )
    }
}

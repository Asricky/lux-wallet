package com.luxwallet.app.feature.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.AccountEntity
import com.luxwallet.app.core.database.entity.CategoryEntity
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.model.TransactionType
import com.luxwallet.app.data.AccountRepository
import com.luxwallet.app.data.CategoryRepository
import com.luxwallet.app.data.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class QuickAddKind { EXPENSE, INCOME, TRANSFER, BALANCE_ADJUSTMENT }

data class QuickAddOptions(val accounts: List<AccountEntity> = emptyList(), val categories: List<CategoryEntity> = emptyList())

class QuickAddViewModel(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    val saving = kotlinx.coroutines.flow.MutableStateFlow(false)
    val error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    val options: StateFlow<QuickAddOptions> = combine(
        accountRepository.observeActiveAccounts(), categoryRepository.observeMainCategories()
    ) { accounts, categories -> QuickAddOptions(accounts, categories) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuickAddOptions())

    fun submit(
        kind: QuickAddKind,
        amount: Long,
        accountId: Long,
        destinationAccountId: Long?,
        categoryId: Long?,
        merchantName: String?,
        note: String?,
        adjustmentIncreasesBalance: Boolean = true,
        onSaved: () -> Unit = {}
    ) {
        if (saving.value) return
        if (amount <= 0 || (kind == QuickAddKind.TRANSFER && (destinationAccountId == null || destinationAccountId == accountId))) {
            error.value = "Isi nominal positif dan pilih akun tujuan yang berbeda."
            return
        }
        saving.value = true
        error.value = null
        viewModelScope.launch {
          try {
            when (kind) {
                QuickAddKind.EXPENSE -> transactionRepository.insertManual(
                    TransactionType.EXPENSE, TransactionDirection.OUT, amount, accountId,
                    categoryId = categoryId, merchantName = merchantName, note = note
                )
                QuickAddKind.INCOME -> transactionRepository.insertManual(
                    TransactionType.INCOME, TransactionDirection.IN, amount, accountId,
                    categoryId = categoryId, merchantName = merchantName, note = note
                )
                QuickAddKind.TRANSFER -> destinationAccountId?.let {
                    transactionRepository.insertManual(
                        TransactionType.INTERNAL_TRANSFER, TransactionDirection.OUT, amount, accountId,
                        destinationAccountId = it, note = note
                    )
                }
                QuickAddKind.BALANCE_ADJUSTMENT -> transactionRepository.insertManual(
                    TransactionType.BALANCE_ADJUSTMENT,
                    if (adjustmentIncreasesBalance) TransactionDirection.IN else TransactionDirection.OUT,
                    amount, accountId, note = note
                )
            }
            onSaved()
          } catch (cancelled: kotlinx.coroutines.CancellationException) {
              throw cancelled
          } catch (failure: Exception) {
              error.value = "Transaksi belum tersimpan. Periksa data dan coba lagi."
          } finally { saving.value = false }
        }
    }

    companion object {
        fun create(app: LuxWalletApp) = QuickAddViewModel(app.accountRepository, app.categoryRepository, app.transactionRepository)
    }
}

package com.luxwallet.app.feature.cashflow

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.CashflowMath
import com.luxwallet.app.core.common.MonthRange
import com.luxwallet.app.core.database.entity.AccountEntity
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.data.AccountRepository
import com.luxwallet.app.data.CategoryRepository
import com.luxwallet.app.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

enum class CashflowTab { EXPENSE, INCOME }

data class CategorySlice(val name: String, val amount: Long, val percent: Int, val color: Color)

data class CashflowUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val tab: CashflowTab = CashflowTab.EXPENSE,
    val accounts: List<AccountEntity> = emptyList(),
    val selectedAccountId: Long? = null, // null = Semua Akun
    val total: Long = 0,
    val categories: List<CategorySlice> = emptyList(),
    val isLoading: Boolean = true
)

class CashflowViewModel(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _controls = MutableStateFlow(Triple(YearMonth.now(), CashflowTab.EXPENSE, null as Long?))
    private val _uiState = MutableStateFlow(CashflowUiState())
    val uiState: StateFlow<CashflowUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                _controls,
                accountRepository.observeActiveAccounts(),
                categoryRepository.observeAll()
            ) { controls, accounts, categories -> Triple(controls, accounts, categories) }
                .collect { (controls, accounts, categories) ->
                    val (yearMonth, tab, selectedAccountId) = controls
                    val range = MonthRange.of(yearMonth)
                    var monthTx = transactionRepository.getInRange(range.startInclusiveMillis, range.endExclusiveMillis)
                    if (selectedAccountId != null) {
                        monthTx = monthTx.filter { it.sourceAccountId == selectedAccountId || it.destinationAccountId == selectedAccountId }
                    }

                    val categoryName: (Long?) -> String = { id -> categories.firstOrNull { it.id == id }?.name ?: "Uncategorized" }

                    val (total, grouped) = when (tab) {
                        CashflowTab.EXPENSE -> CashflowMath.totalExpense(monthTx) to CashflowMath.expenseByCategory(monthTx, categoryName)
                        CashflowTab.INCOME -> CashflowMath.totalIncome(monthTx) to CashflowMath.incomeByCategory(monthTx, categoryName)
                    }

                    val palette = listOf(
                        Color(0xFF1B3F57), Color(0xFFF2C744), Color(0xFF3457A6), Color(0xFF0F7B44),
                        Color(0xFF8A5A00), Color(0xFF5B6570), Color(0xFF9C6ADE), Color(0xFFE07A5F)
                    )
                    val safeTotal = total.coerceAtLeast(1)
                    val slices = grouped.mapIndexed { index, (name, amount) ->
                        CategorySlice(name, amount, ((amount * 100) / safeTotal).toInt(), palette[index % palette.size])
                    }

                    _uiState.value = CashflowUiState(
                        yearMonth = yearMonth, tab = tab, accounts = accounts, selectedAccountId = selectedAccountId,
                        total = total, categories = slices, isLoading = false
                    )
                }
        }
    }

    fun setMonth(yearMonth: YearMonth) = _controls.update { it.copy(first = yearMonth) }
    fun setTab(tab: CashflowTab) = _controls.update { it.copy(second = tab) }
    fun setAccountFilter(accountId: Long?) = _controls.update { it.copy(third = accountId) }

    companion object {
        fun create(app: LuxWalletApp) = CashflowViewModel(app.accountRepository, app.categoryRepository, app.transactionRepository)
    }
}

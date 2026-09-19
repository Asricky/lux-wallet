package com.luxwallet.app.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.CashflowMath
import com.luxwallet.app.core.common.MonthRange
import com.luxwallet.app.core.database.entity.BudgetEntity
import com.luxwallet.app.core.database.entity.FinancialProfileEntity
import com.luxwallet.app.core.database.entity.GoalEntity
import com.luxwallet.app.core.model.BudgetKind
import com.luxwallet.app.core.model.BudgetStatus
import com.luxwallet.app.data.BudgetRepository
import com.luxwallet.app.data.CategoryRepository
import com.luxwallet.app.data.FinancialProfileRepository
import com.luxwallet.app.data.GoalRepository
import com.luxwallet.app.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.YearMonth

data class BudgetLine(val categoryName: String, val budgetAmount: Long, val spent: Long, val status: BudgetStatus)

data class BudgetGoalsUiState(
    val profile: FinancialProfileEntity = FinancialProfileEntity(baselineDate = 0),
    val discretionarySpent: Long = 0,
    val overallBudgetLine: BudgetLine? = null,
    val categoryLines: List<BudgetLine> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val isLoading: Boolean = true
)

class BudgetGoalsViewModel(
    private val financialProfileRepository: FinancialProfileRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetGoalsUiState())
    val uiState: StateFlow<BudgetGoalsUiState> = _uiState

    private val currentYearMonth = YearMonth.now().toString() // "yyyy-MM"

    init {
        viewModelScope.launch {
            combine(
                financialProfileRepository.observe(),
                budgetRepository.observeForMonth(currentYearMonth),
                goalRepository.observeAll(),
                categoryRepository.observeAll()
            ) { profile, budgets, goals, categories ->
                val range = MonthRange.of(YearMonth.now())
                val monthTx = transactionRepository.getInRange(range.startInclusiveMillis, range.endExclusiveMillis)
                val categoryName: (Long?) -> String = { id -> categories.firstOrNull { it.id == id }?.name ?: "Uncategorized" }
                val expenseByCategory = CashflowMath.expenseByCategory(monthTx, categoryName).toMap()
                val totalExpense = CashflowMath.totalExpense(monthTx)

                val overall = budgets.firstOrNull { it.kind == BudgetKind.OVERALL }
                    ?.let { budgetLine("Overall", it.amount, totalExpense) }

                val categoryLines = budgets.filter { it.kind == BudgetKind.CATEGORY }.map {
                    val name = categories.firstOrNull { c -> c.id == it.categoryId }?.name ?: "Uncategorized"
                    budgetLine(name, it.amount, expenseByCategory[name] ?: 0)
                }

                BudgetGoalsUiState(profile, totalExpense, overall, categoryLines, goals, isLoading = false)
            }.collect { _uiState.value = it }
        }
    }

    private fun budgetLine(name: String, budget: Long, spent: Long): BudgetLine {
        if (budget <= 0) return BudgetLine(name, budget, spent, BudgetStatus.ON_TRACK)
        val percent = spent.toDouble() / budget
        val status = when {
            percent >= 1.0 -> BudgetStatus.OVER_BUDGET
            percent >= 0.8 -> BudgetStatus.WATCH
            else -> BudgetStatus.ON_TRACK
        }
        return BudgetLine(name, budget, spent, status)
    }

    fun saveProfile(profile: FinancialProfileEntity) {
        viewModelScope.launch { financialProfileRepository.save(profile) }
    }

    fun setOverallBudget(amount: Long) {
        viewModelScope.launch {
            budgetRepository.upsert(BudgetEntity(kind = BudgetKind.OVERALL, categoryId = null, yearMonth = currentYearMonth, amount = amount))
        }
    }

    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch { goalRepository.upsert(goal) }
    }

    companion object {
        fun create(app: LuxWalletApp) = BudgetGoalsViewModel(
            app.financialProfileRepository, app.budgetRepository, app.goalRepository, app.categoryRepository, app.transactionRepository
        )
    }
}

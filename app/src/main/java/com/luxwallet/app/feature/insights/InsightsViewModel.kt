package com.luxwallet.app.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.CashflowMath
import com.luxwallet.app.core.common.MonthRange
import com.luxwallet.app.data.AccountRepository
import com.luxwallet.app.data.CategoryRepository
import com.luxwallet.app.data.FinancialProfileRepository
import com.luxwallet.app.data.GoalRepository
import com.luxwallet.app.data.TransactionRepository
import com.luxwallet.app.engine.CategorySpend
import com.luxwallet.app.engine.GuidanceItem
import com.luxwallet.app.engine.Insight
import com.luxwallet.app.engine.InsightsEngine
import com.luxwallet.app.engine.InsightsInput
import com.luxwallet.app.engine.InvestmentGuidanceEngine
import com.luxwallet.app.engine.InvestmentGuidanceInput
import com.luxwallet.app.engine.SafeToSpendEngine
import com.luxwallet.app.engine.SafeToSpendInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class InsightsUiState(
    val insights: List<Insight> = emptyList(),
    val guidancePriority: String = "",
    val guidanceItems: List<GuidanceItem> = emptyList(),
    val emergencyFundMonths: Double = 0.0,
    val isLoading: Boolean = true
)

/**
 * PRD §35: deterministic insights, comparing this month's category spend against the trailing
 * 3-month average, discretionary budget usage, and cash allocation vs target.
 */
class InsightsViewModel(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val financialProfileRepository: FinancialProfileRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState

    init {
        // One-shot compute per ViewModel instance; avoids an unbounded reactive combine over 4 months of history.
        viewModelScope.launch { recompute() }
    }

    private suspend fun recompute() {
        val categories = categoryNameLookup()
        val now = YearMonth.now()

        val currentRange = MonthRange.of(now)
        val currentTx = transactionRepository.getInRange(currentRange.startInclusiveMillis, currentRange.endExclusiveMillis)
        val currentByCategory = CashflowMath.expenseByCategory(currentTx, categories).toMap()

        val trailingTotals = mutableMapOf<String, Long>()
        for (i in 1..3) {
            val range = MonthRange.of(now.minusMonths(i.toLong()))
            val tx = transactionRepository.getInRange(range.startInclusiveMillis, range.endExclusiveMillis)
            CashflowMath.expenseByCategory(tx, categories).forEach { (name, amount) ->
                trailingTotals[name] = (trailingTotals[name] ?: 0) + amount
            }
        }

        val categorySpends = currentByCategory.map { (name, amount) ->
            CategorySpend(name, amount, (trailingTotals[name] ?: 0) / 3)
        }

        val profile = financialProfileRepository.get()
        val totalExpense = CashflowMath.totalExpense(currentTx)
        val safeToSpend = SafeToSpendEngine.calculate(
            SafeToSpendInput(
                expectedMonthlyIncome = profile.expectedMonthlyIncome,
                fixedObligations = profile.fixedObligations,
                debtPayments = profile.debtPayments,
                savingsTarget = profile.savingsTargetMonthly,
                investmentTarget = profile.investmentTargetMonthly,
                plannedExpenses = profile.plannedExpensesMonthly,
                safetyBuffer = profile.safetyBufferMonthly,
                discretionarySpentThisMonthSoFar = totalExpense,
                today = LocalDate.now()
            )
        )

        val insights = InsightsEngine.generate(
            InsightsInput(
                categorySpends = categorySpends,
                discretionaryBudget = safeToSpend.monthlySpendableBudget,
                discretionarySpent = totalExpense,
                cashAllocationPercent = null,
                cashAllocationTargetRange = null
            )
        )

        val accounts = accountRepository.observeActiveAccounts().first()
        val liquidAssets = accounts.filter { it.includeInNetWorth }.sumOf { it.currentEstimatedBalance }
        val goals = goalRepository.observeAll().first()
        val nearestGoalHorizonMonths = goals
            .mapNotNull { it.targetDate }
            .minOrNull()
            ?.let { targetMillis ->
                val months = ChronoUnit.MONTHS.between(LocalDate.now(), java.time.Instant.ofEpochMilli(targetMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                months.toInt().coerceAtLeast(0)
            }

        val monthlyExpenseEstimate = if (totalExpense > 0) totalExpense else (trailingTotals.values.sum() / 3).coerceAtLeast(1)

        val guidance = InvestmentGuidanceEngine.assess(
            InvestmentGuidanceInput(
                monthlyIncome = profile.expectedMonthlyIncome,
                monthlyExpenses = monthlyExpenseEstimate,
                monthlyDebtPayments = profile.debtPayments,
                liquidAssets = liquidAssets,
                emergencyFundMonthsTarget = profile.emergencyFundMonthsTarget,
                riskProfile = profile.riskProfile,
                nearestGoalHorizonMonths = nearestGoalHorizonMonths
            )
        )

        _uiState.value = InsightsUiState(
            insights = insights,
            guidancePriority = guidance.priorityStatement,
            guidanceItems = guidance.items,
            emergencyFundMonths = guidance.emergencyFundMonthsCurrent,
            isLoading = false
        )
    }

    private suspend fun categoryNameLookup(): (Long?) -> String {
        // Use a synchronous snapshot via first() since category set rarely changes mid-session.
        val list = categoryRepository.observeAll().first()
        val map = list.associate { it.id to it.name }
        return { id -> map[id] ?: "Uncategorized" }
    }

    companion object {
        fun create(app: LuxWalletApp) = InsightsViewModel(
            app.accountRepository, app.categoryRepository, app.transactionRepository, app.financialProfileRepository, app.goalRepository
        )
    }
}

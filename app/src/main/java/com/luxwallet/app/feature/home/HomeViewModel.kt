package com.luxwallet.app.feature.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.CashflowMath
import com.luxwallet.app.core.common.MonthRange
import com.luxwallet.app.core.database.entity.AccountEntity
import com.luxwallet.app.core.ui.component.DonutSlice
import com.luxwallet.app.data.AccountRepository
import com.luxwallet.app.data.AssetRepository
import com.luxwallet.app.data.FinancialProfileRepository
import com.luxwallet.app.data.LiabilityRepository
import com.luxwallet.app.data.TransactionRepository
import com.luxwallet.app.engine.CategorySpend
import com.luxwallet.app.engine.Insight
import com.luxwallet.app.engine.InsightsEngine
import com.luxwallet.app.engine.InsightsInput
import com.luxwallet.app.engine.NetWorthEngine
import com.luxwallet.app.engine.NetWorthInput
import com.luxwallet.app.engine.SafeToSpendEngine
import com.luxwallet.app.engine.SafeToSpendInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class HomeUiState(
    val netWorth: Long = 0,
    val netWorthChangeThisMonth: Long = 0,
    val todaySpent: Long = 0,
    val safeToSpendToday: Long = 0,
    val remainingToday: Long = 0,
    val isOverBudget: Boolean = false,
    val monthIncome: Long = 0,
    val monthExpense: Long = 0,
    val savingsRatePercent: Int = 0,
    val assetDistribution: List<DonutSlice> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val hasAccounts: Boolean = false,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val accountRepository: AccountRepository,
    private val assetRepository: AssetRepository,
    private val liabilityRepository: LiabilityRepository,
    private val transactionRepository: TransactionRepository,
    private val financialProfileRepository: FinancialProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                accountRepository.observeActiveAccounts(),
                assetRepository.observeAll(),
                liabilityRepository.observeAll(),
                financialProfileRepository.observe()
            ) { accounts, assets, liabilities, profile ->
                    HomeInputs(accounts, assets, liabilities, profile)
            }.combine(transactionRepository.observeAll()) { inputs, transactions -> inputs to transactions }
                .collect { (inputs, transactions) ->
                    val (accounts, assets, liabilities, profile) = inputs
                    val zone = ZoneId.systemDefault()
                    val today = LocalDate.now(zone)
                    val thisMonth = MonthRange.currentMonth(zone)
                    val monthTx = transactions.filter { it.transactionTime >= thisMonth.startInclusiveMillis && it.transactionTime < thisMonth.endExclusiveMillis }
                    val cashflowTx = CashflowMath.cashflowEligible(monthTx)

                    val netWorthResult = NetWorthEngine.calculate(
                        NetWorthInput(
                            accountBalances = accounts.filter { it.includeInNetWorth }.map { it.currentEstimatedBalance },
                            otherAssetValues = assets.filter { it.includeInNetWorth }.map { it.currentValue },
                            liabilityOutstanding = liabilities.map { it.currentOutstanding }
                        )
                    )

                    val monthIncome = CashflowMath.totalIncome(monthTx)
                    val monthExpense = CashflowMath.totalExpense(monthTx)

                    val todayStartMillis = today.atStartOfDay(zone).toInstant().toEpochMilli()
                    val todaySpent = cashflowTx
                        .filter { it.transactionTime >= todayStartMillis && it.direction == com.luxwallet.app.core.model.TransactionDirection.OUT }
                        .sumOf { it.amount }

                    val safeToSpend = SafeToSpendEngine.calculate(
                        SafeToSpendInput(
                            expectedMonthlyIncome = profile.expectedMonthlyIncome,
                            fixedObligations = profile.fixedObligations,
                            debtPayments = profile.debtPayments,
                            savingsTarget = profile.savingsTargetMonthly,
                            investmentTarget = profile.investmentTargetMonthly,
                            plannedExpenses = profile.plannedExpensesMonthly,
                            safetyBuffer = profile.safetyBufferMonthly,
                            discretionarySpentThisMonthSoFar = monthExpense - todaySpent,
                            today = today
                        )
                    )

                    val savingsRate = if (monthIncome > 0) (((monthIncome - monthExpense).toDouble() / monthIncome) * 100).toInt() else 0

                    val distribution = buildDistribution(accounts, assets)

                    val insights = InsightsEngine.generate(
                        InsightsInput(
                            categorySpends = emptyList(), // trailing-average history not yet queried in this MVP pass
                            discretionaryBudget = safeToSpend.monthlySpendableBudget,
                            discretionarySpent = monthExpense,
                            cashAllocationPercent = null,
                            cashAllocationTargetRange = null
                        )
                    )

                    _uiState.value = HomeUiState(
                        netWorth = netWorthResult.netWorth,
                        netWorthChangeThisMonth = monthIncome - monthExpense,
                        todaySpent = todaySpent,
                        safeToSpendToday = safeToSpend.safeToSpendToday,
                        remainingToday = safeToSpend.safeToSpendToday - todaySpent,
                        isOverBudget = safeToSpend.isOverBudget,
                        monthIncome = monthIncome,
                        monthExpense = monthExpense,
                        savingsRatePercent = savingsRate,
                        assetDistribution = distribution,
                        insights = insights,
                        hasAccounts = accounts.isNotEmpty(),
                        isLoading = false
                    )
                }
        }
    }

    private fun buildDistribution(accounts: List<AccountEntity>, assets: List<com.luxwallet.app.core.database.entity.AssetEntity>): List<DonutSlice> {
        val palette = listOf(
            Color(0xFF1B3F57), Color(0xFFF2C744), Color(0xFF3457A6),
            Color(0xFF0F7B44), Color(0xFF8A5A00), Color(0xFF5B6570)
        )
        val entries = mutableListOf<Pair<String, Long>>()
        accounts.filter { it.includeInNetWorth && it.currentEstimatedBalance > 0 }.forEach { entries += it.name to it.currentEstimatedBalance }
        val assetsTotal = assets.filter { it.includeInNetWorth }.sumOf { it.currentValue }
        if (assetsTotal > 0) entries += "Investments & Other Assets" to assetsTotal

        return entries.mapIndexed { index, (name, value) ->
            DonutSlice(label = name, value = value.toDouble(), color = palette[index % palette.size])
        }
    }

    private data class HomeInputs(
        val accounts: List<AccountEntity>,
        val assets: List<com.luxwallet.app.core.database.entity.AssetEntity>,
        val liabilities: List<com.luxwallet.app.core.database.entity.LiabilityEntity>,
        val profile: com.luxwallet.app.core.database.entity.FinancialProfileEntity
    )

    companion object {
        fun create(app: LuxWalletApp) = HomeViewModel(
            app.accountRepository, app.assetRepository, app.liabilityRepository,
            app.transactionRepository, app.financialProfileRepository
        )
    }
}

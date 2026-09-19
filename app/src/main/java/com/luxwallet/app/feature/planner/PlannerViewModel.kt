package com.luxwallet.app.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.TransportPlan
import com.luxwallet.app.core.database.entity.*
import com.luxwallet.app.engine.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PlannerState(
    val plans: List<PaydayPlan> = emptyList(), val transactions: List<TransactionEntity> = emptyList(),
    val transportIds: Set<Long> = emptySet(), val billIds: Set<Long> = emptySet(),
    val accountCash: Long = 0, val today: LocalDate = LocalDate.now(), val loaded: Boolean = false
) {
    val latest get() = plans.maxByOrNull { it.capturedAt }
    val status get() = latest?.let { PaydayMath.status(it, transactions, transportIds, billIds, today) }
}
class PlannerViewModel(private val app: LuxWalletApp) : ViewModel() {
    private val date = flow { while (true) { emit(LocalDate.now()); kotlinx.coroutines.delay(60_000) } }.distinctUntilChanged()
    val state = combine(app.paydayPlanRepository.plans, app.transactionRepository.observeAll(),
        app.categoryRepository.observeAll(), app.accountRepository.observeActiveAccounts(), date) { plans, txs, categories, accounts, today ->
        PlannerState(plans, txs, categories.filter { it.name == TransportPlan.CATEGORY }.map { it.id }.toSet(),
            categories.filter { it.name == PaydayPlan.BILLS_CATEGORY }.map { it.id }.toSet(),
            accounts.filter { it.isOwnedByUser }.sumOf { it.currentEstimatedBalance }, today, true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlannerState())
    val saving = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)
    fun save(plan: PaydayPlan, onSaved: () -> Unit) {
        if (saving.value) return
        saving.value = true
        viewModelScope.launch {
            try { app.paydayPlanRepository.save(plan); message.value = "Rencana tersimpan. Saldo rekening tidak diubah."; onSaved() }
            catch (e: Exception) { message.value = e.message ?: "Rencana belum tersimpan. Coba lagi." }
            finally { saving.value = false }
        }
    }
}

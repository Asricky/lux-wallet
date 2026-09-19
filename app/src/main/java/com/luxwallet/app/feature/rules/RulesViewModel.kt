package com.luxwallet.app.feature.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.CategoryEntity
import com.luxwallet.app.core.database.entity.MerchantRuleEntity
import com.luxwallet.app.data.CategoryRepository
import com.luxwallet.app.data.MerchantRuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class RulesUiState(val rules: List<MerchantRuleEntity> = emptyList(), val categories: List<CategoryEntity> = emptyList())

class RulesViewModel(
    private val merchantRuleRepository: MerchantRuleRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RulesUiState())
    val uiState: StateFlow<RulesUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(merchantRuleRepository.observeAll(), categoryRepository.observeMainCategories()) { rules, categories ->
                RulesUiState(rules, categories)
            }.collect { _uiState.value = it }
        }
    }

    fun addRule(merchantContains: String, categoryId: Long) {
        if (merchantContains.isBlank()) return
        viewModelScope.launch { merchantRuleRepository.learnRule(merchantContains, categoryId, null) }
    }

    fun deleteRule(rule: MerchantRuleEntity) {
        viewModelScope.launch { merchantRuleRepository.delete(rule) }
    }

    companion object {
        fun create(app: LuxWalletApp) = RulesViewModel(app.merchantRuleRepository, app.categoryRepository)
    }
}

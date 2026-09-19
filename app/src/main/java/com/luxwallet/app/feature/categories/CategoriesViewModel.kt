package com.luxwallet.app.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.CategoryEntity
import com.luxwallet.app.data.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeMainCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { categoryRepository.resolveOrCreateTopLevel(name) }
    }

    companion object {
        fun create(app: LuxWalletApp) = CategoriesViewModel(app.categoryRepository)
    }
}

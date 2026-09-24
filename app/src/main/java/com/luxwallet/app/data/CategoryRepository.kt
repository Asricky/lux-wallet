package com.luxwallet.app.data

import com.luxwallet.app.core.database.DefaultCategories
import com.luxwallet.app.core.database.dao.CategoryDao
import com.luxwallet.app.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun observeMainCategories(): Flow<List<CategoryEntity>> = categoryDao.observeMainCategories().map(::prioritizeCategories)
    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll().map(::prioritizeCategories)

    suspend fun seedDefaultsIfEmpty() {
        if (categoryDao.count() > 0) return
        val mainIds = mutableMapOf<String, Long>()
        for (name in DefaultCategories.MAIN_CATEGORIES) {
            val id = categoryDao.insert(
                CategoryEntity(name = name, parentId = null, isDefault = true, isIncomeCategory = name in DefaultCategories.INCOME_CATEGORY_NAMES)
            )
            mainIds[name] = id
        }
        for ((mainName, subs) in DefaultCategories.SUBCATEGORIES) {
            val parentId = mainIds[mainName] ?: continue
            for (subName in subs) {
                categoryDao.insert(CategoryEntity(name = subName, parentId = parentId, isDefault = true))
            }
        }
    }

    /** Finds an existing top-level category by (case-insensitive) name, or creates one from evidence-based notification text. */
    suspend fun resolveOrCreateTopLevel(name: String, isIncome: Boolean = false): Long {
        val normalized = name.trim()
        categoryDao.findMainCategoryByName(normalized)?.let { return it.id }
        val existing = categoryDao.getAll().firstOrNull { it.parentId == null && it.name.equals(normalized, ignoreCase = true) }
        if (existing != null) return existing.id
        return categoryDao.insert(CategoryEntity(name = normalized, parentId = null, isDefault = false, isIncomeCategory = isIncome))
    }

    suspend fun getById(id: Long) = categoryDao.getById(id)
}

fun prioritizeCategories(categories: List<CategoryEntity>) = categories.sortedWith(
    compareBy<CategoryEntity> { if (it.name.trim().equals("Food & Drink", ignoreCase = true)) 0 else 1 }
        .thenBy { it.name.lowercase(java.util.Locale.ROOT) })

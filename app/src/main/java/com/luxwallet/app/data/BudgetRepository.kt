package com.luxwallet.app.data

import com.luxwallet.app.core.database.dao.BudgetDao
import com.luxwallet.app.core.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {
    fun observeForMonth(yearMonth: String): Flow<List<BudgetEntity>> = budgetDao.observeForMonth(yearMonth)
    suspend fun getForMonth(yearMonth: String): List<BudgetEntity> = budgetDao.getForMonth(yearMonth)
    suspend fun upsert(budget: BudgetEntity) = budgetDao.upsert(budget)
    suspend fun delete(budget: BudgetEntity) = budgetDao.delete(budget)
}

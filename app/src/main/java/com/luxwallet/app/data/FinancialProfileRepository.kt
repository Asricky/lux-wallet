package com.luxwallet.app.data

import com.luxwallet.app.core.database.dao.FinancialProfileDao
import com.luxwallet.app.core.database.entity.FinancialProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinancialProfileRepository(private val financialProfileDao: FinancialProfileDao) {
    fun observe(): Flow<FinancialProfileEntity> =
        financialProfileDao.observe().map { it ?: FinancialProfileEntity(baselineDate = System.currentTimeMillis()) }

    suspend fun get(): FinancialProfileEntity =
        financialProfileDao.get() ?: FinancialProfileEntity(baselineDate = System.currentTimeMillis())

    suspend fun save(profile: FinancialProfileEntity) = financialProfileDao.upsert(profile)
}

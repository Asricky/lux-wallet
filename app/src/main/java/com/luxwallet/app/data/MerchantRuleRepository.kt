package com.luxwallet.app.data

import com.luxwallet.app.core.database.dao.MerchantRuleDao
import com.luxwallet.app.core.database.entity.MerchantRuleEntity
import kotlinx.coroutines.flow.Flow

class MerchantRuleRepository(private val merchantRuleDao: MerchantRuleDao) {
    fun observeAll(): Flow<List<MerchantRuleEntity>> = merchantRuleDao.observeAll()

    /** PRD §23 "User correction learning": called when the user changes a transaction's category. */
    suspend fun learnRule(merchantContains: String, categoryId: Long, subcategoryId: Long?) {
        merchantRuleDao.insert(
            MerchantRuleEntity(
                merchantContains = merchantContains,
                categoryId = categoryId,
                subcategoryId = subcategoryId,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun delete(rule: MerchantRuleEntity) = merchantRuleDao.delete(rule)
}

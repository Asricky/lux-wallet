package com.luxwallet.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.luxwallet.app.core.database.entity.MerchantRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantRuleDao {
    @Insert
    suspend fun insert(rule: MerchantRuleEntity): Long

    @Delete
    suspend fun delete(rule: MerchantRuleEntity)

    @Query("SELECT * FROM merchant_rules ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MerchantRuleEntity>>

    @Query("SELECT * FROM merchant_rules")
    suspend fun getAll(): List<MerchantRuleEntity>
}

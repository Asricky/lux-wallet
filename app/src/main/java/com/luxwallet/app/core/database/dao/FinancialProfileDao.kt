package com.luxwallet.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luxwallet.app.core.database.entity.FinancialProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: FinancialProfileEntity)

    @Query("SELECT * FROM financial_profile WHERE id = ${FinancialProfileEntity.SINGLETON_ID}")
    fun observe(): Flow<FinancialProfileEntity?>

    @Query("SELECT * FROM financial_profile WHERE id = ${FinancialProfileEntity.SINGLETON_ID}")
    suspend fun get(): FinancialProfileEntity?
}

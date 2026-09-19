package com.luxwallet.app.core.database.dao

import androidx.room.*
import com.luxwallet.app.core.database.entity.PaydayPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao interface PaydayPlanDao {
    @Query("SELECT * FROM payday_plans ORDER BY capturedAt") fun observeAll(): Flow<List<PaydayPlanEntity>>
    @Query("SELECT * FROM payday_plans ORDER BY capturedAt") suspend fun getAllOnce(): List<PaydayPlanEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(plan: PaydayPlanEntity)
}

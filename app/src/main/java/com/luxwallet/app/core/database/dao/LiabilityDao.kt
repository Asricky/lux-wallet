package com.luxwallet.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.luxwallet.app.core.database.entity.LiabilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LiabilityDao {
    @Insert
    suspend fun insert(liability: LiabilityEntity): Long

    @Update
    suspend fun update(liability: LiabilityEntity)

    @Delete
    suspend fun delete(liability: LiabilityEntity)

    @Query("SELECT * FROM liabilities ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<LiabilityEntity>>

    @Query("SELECT * FROM liabilities")
    suspend fun getAllOnce(): List<LiabilityEntity>

    @Query("SELECT COALESCE(SUM(currentOutstanding), 0) FROM liabilities")
    fun observeTotalOutstanding(): Flow<Long>
}

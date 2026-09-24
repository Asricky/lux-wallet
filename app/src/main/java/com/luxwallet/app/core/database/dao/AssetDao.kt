package com.luxwallet.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.luxwallet.app.core.database.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("UPDATE assets SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)
    @Query("SELECT * FROM assets WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun observeArchived(): Flow<List<AssetEntity>>

    @Insert
    suspend fun insert(asset: AssetEntity): Long

    @Update
    suspend fun update(asset: AssetEntity)

    @Delete
    suspend fun delete(asset: AssetEntity)

    @Query("SELECT * FROM assets WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets")
    suspend fun getAllOnce(): List<AssetEntity>

    @Query("SELECT COALESCE(SUM(currentValue), 0) FROM assets WHERE includeInNetWorth = 1 AND isArchived = 0")
    fun observeTotalValue(): Flow<Long>
}

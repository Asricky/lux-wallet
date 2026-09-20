package com.luxwallet.app.core.database.dao

import androidx.room.*
import com.luxwallet.app.core.database.entity.TransactionConfirmationEntity
import kotlinx.coroutines.flow.Flow

@Dao interface TransactionConfirmationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: TransactionConfirmationEntity)
    @Query("UPDATE transaction_confirmations SET dueAt = :now WHERE transactionId = :id AND handled = 0")
    suspend fun ready(id: Long, now: Long)
    @Query("SELECT MIN(dueAt) FROM transaction_confirmations WHERE handled = 0")
    fun nextDue(): Flow<Long?>
    @Query("SELECT * FROM transaction_confirmations WHERE handled = 0 AND dueAt <= :now ORDER BY dueAt")
    suspend fun due(now: Long): List<TransactionConfirmationEntity>
    @Query("UPDATE transaction_confirmations SET handled = 1 WHERE transactionId = :id")
    suspend fun handled(id: Long)
    @Query("SELECT * FROM transaction_confirmations WHERE transactionId = :id")
    suspend fun get(id: Long): TransactionConfirmationEntity?
    @Query("DELETE FROM transaction_confirmations")
    suspend fun clear()
}

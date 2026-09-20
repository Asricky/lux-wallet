package com.luxwallet.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.ReviewStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT COALESCE(MAX(id), 0) FROM transactions")
    suspend fun lastId(): Long

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY transactionTime DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAllOnce(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE reviewStatus = :status ORDER BY transactionTime DESC")
    fun observeByReviewStatus(status: ReviewStatus): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE transactionTime >= :fromInclusive AND transactionTime < :toExclusive " +
            "ORDER BY transactionTime DESC"
    )
    fun observeInRange(fromInclusive: Long, toExclusive: Long): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE transactionTime >= :fromInclusive AND transactionTime < :toExclusive " +
            "ORDER BY transactionTime DESC"
    )
    suspend fun getInRange(fromInclusive: Long, toExclusive: Long): List<TransactionEntity>

    /** Unmatched candidates near a given time window, for the [com.luxwallet.app.engine.MatchingEngine] to score in Kotlin. */
    @Query(
        "SELECT * FROM transactions WHERE isInternalTransfer = 0 AND reviewStatus != 'IGNORED' " +
            "AND isManual = 0 AND type IN ('EXTERNAL_TRANSFER', 'EWALLET_TOPUP') " +
            "AND transactionTime BETWEEN :fromTime AND :toTime ORDER BY transactionTime ASC"
    )
    suspend fun findUnmatchedInWindow(fromTime: Long, toTime: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isManual = 0 AND reviewStatus != 'IGNORED' ORDER BY transactionTime DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<TransactionEntity>
}

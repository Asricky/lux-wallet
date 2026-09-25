package com.luxwallet.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.luxwallet.app.core.database.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerEntryDao {
    @Insert
    suspend fun insert(entry: LedgerEntryEntity): Long

    @Insert
    suspend fun insertAll(entries: List<LedgerEntryEntity>)

    @Query("DELETE FROM ledger_entries WHERE transactionId = :transactionId")
    suspend fun deleteForTransaction(transactionId: Long)

    @Query("SELECT * FROM ledger_entries WHERE accountId = :accountId ORDER BY createdAt DESC")
    fun observeForAccount(accountId: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE transactionId = :transactionId")
    suspend fun getForTransaction(transactionId: Long): List<LedgerEntryEntity>

    @Query("SELECT * FROM ledger_entries")
    suspend fun getAllOnce(): List<LedgerEntryEntity>

    @Query("SELECT COALESCE(SUM(deltaAmount), 0) FROM ledger_entries WHERE accountId = :accountId")
    suspend fun sumForAccount(accountId: Long): Long
}

package com.luxwallet.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.luxwallet.app.core.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY name ASC")
    fun observeActiveAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccountsOnce(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE provider = :provider AND isOwnedByUser = 1 AND isActive = 1 LIMIT 1")
    suspend fun findUserOwnedByProvider(provider: com.luxwallet.app.core.model.AccountProvider): AccountEntity?

    @Query("SELECT * FROM accounts WHERE provider = :provider AND isOwnedByUser = 1 AND isActive = 1")
    suspend fun findAllUserOwnedByProvider(provider: com.luxwallet.app.core.model.AccountProvider): List<AccountEntity>

    @Query("UPDATE accounts SET currentEstimatedBalance = currentEstimatedBalance + :delta WHERE id = :accountId")
    suspend fun applyBalanceDelta(accountId: Long, delta: Long)

    @Query("UPDATE accounts SET currentEstimatedBalance = :balance, lastReconciledAt = :reconciledAt WHERE id = :accountId")
    suspend fun reconcileBalance(accountId: Long, balance: Long, reconciledAt: Long)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int
}

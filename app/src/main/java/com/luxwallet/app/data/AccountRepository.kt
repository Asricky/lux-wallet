package com.luxwallet.app.data

import com.luxwallet.app.core.database.dao.AccountDao
import com.luxwallet.app.core.database.entity.AccountEntity
import com.luxwallet.app.core.model.AccountKind
import com.luxwallet.app.core.model.AccountProvider
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {

    fun observeActiveAccounts(): Flow<List<AccountEntity>> = accountDao.observeActiveAccounts()
    fun observeAllAccounts(): Flow<List<AccountEntity>> = accountDao.observeAllAccounts()

    suspend fun getById(id: Long) = accountDao.getById(id)

    suspend fun createAccount(
        name: String,
        kind: AccountKind,
        provider: AccountProvider,
        openingBalance: Long,
        openingBalanceDate: Long,
        ownerName: String? = null,
        isOwnedByUser: Boolean = true,
        includeInNetWorth: Boolean = true
    ): Long = accountDao.insert(
        AccountEntity(
            name = name,
            kind = kind,
            provider = provider,
            ownerName = ownerName,
            currentEstimatedBalance = openingBalance,
            openingBalance = openingBalance,
            openingBalanceDate = openingBalanceDate,
            isOwnedByUser = isOwnedByUser,
            includeInNetWorth = includeInNetWorth
        )
    )

    suspend fun update(account: AccountEntity) = accountDao.update(account)

    /** The user-owned account Lux Wallet should attribute a notification from [provider] to, if the user set one up. */
    suspend fun findUserOwnedAccount(provider: AccountProvider): AccountEntity? =
        accountDao.findUserOwnedByProvider(provider)

    suspend fun applyLedgerDelta(accountId: Long, delta: Long) = accountDao.applyBalanceDelta(accountId, delta)

    suspend fun reconcile(accountId: Long, reportedBalance: Long, reconciledAt: Long) =
        accountDao.reconcileBalance(accountId, reportedBalance, reconciledAt)

    suspend fun hasAnyAccounts(): Boolean = accountDao.count() > 0
}

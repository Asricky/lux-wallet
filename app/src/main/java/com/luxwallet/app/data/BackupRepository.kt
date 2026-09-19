package com.luxwallet.app.data

import android.content.Context
import androidx.room.withTransaction
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.luxwallet.app.core.database.LuxDatabase
import com.luxwallet.app.core.database.entity.AccountEntity
import com.luxwallet.app.core.database.entity.AssetEntity
import com.luxwallet.app.core.database.entity.BudgetEntity
import com.luxwallet.app.core.database.entity.CategoryEntity
import com.luxwallet.app.core.database.entity.FinancialProfileEntity
import com.luxwallet.app.core.database.entity.GoalEntity
import com.luxwallet.app.core.database.entity.LedgerEntryEntity
import com.luxwallet.app.core.database.entity.LiabilityEntity
import com.luxwallet.app.core.database.entity.MerchantRuleEntity
import com.luxwallet.app.core.database.entity.TransactionEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Manual encrypted local backup/restore (PRD §37). Raw notifications are intentionally excluded
 * (kept only if the user later opts in) — everything the user actually manages is included:
 * accounts, balances, transactions, assets, liabilities, categories, rules, budgets, goals.
 */
@Serializable
data class BackupPayload(
    val schemaVersion: Int,
    val exportedAt: Long,
    val accounts: List<AccountEntity>,
    val assets: List<AssetEntity>,
    val liabilities: List<LiabilityEntity>,
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val ledgerEntries: List<LedgerEntryEntity>,
    val merchantRules: List<MerchantRuleEntity>,
    val budgets: List<BudgetEntity>,
    val goals: List<GoalEntity>,
    val financialProfile: FinancialProfileEntity?
)

sealed class RestoreResult {
    object Success : RestoreResult()
    data class IncompatibleSchema(val backupVersion: Int, val currentVersion: Int) : RestoreResult()
    data class Failure(val message: String) : RestoreResult()
}

class BackupRepository(private val context: Context, private val database: LuxDatabase) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    suspend fun export(destinationFile: File) {
        val payload = BackupPayload(
            schemaVersion = SCHEMA_VERSION,
            exportedAt = System.currentTimeMillis(),
            accounts = database.accountDao().getAllAccountsOnce(),
            assets = database.assetDao().getAllOnce(),
            liabilities = database.liabilityDao().getAllOnce(),
            categories = database.categoryDao().getAll(),
            transactions = database.transactionDao().getAllOnce(),
            ledgerEntries = database.ledgerEntryDao().getAllOnce(),
            merchantRules = database.merchantRuleDao().getAll(),
            budgets = database.budgetDao().getAllOnce(),
            goals = database.goalDao().getAllOnce(),
            financialProfile = database.financialProfileDao().get()
        )
        val bytes = json.encodeToString(BackupPayload.serializer(), payload).toByteArray(Charsets.UTF_8)

        if (destinationFile.exists()) destinationFile.delete()
        val encryptedFile = buildEncryptedFile(destinationFile)
        encryptedFile.openFileOutput().use { it.write(bytes) }
    }

    suspend fun restore(sourceFile: File): RestoreResult {
        return try {
            val encryptedFile = buildEncryptedFile(sourceFile)
            val bytes = encryptedFile.openFileInput().use { it.readBytes() }
            val payload = json.decodeFromString(BackupPayload.serializer(), String(bytes, Charsets.UTF_8))

            if (payload.schemaVersion != SCHEMA_VERSION) {
                return RestoreResult.IncompatibleSchema(payload.schemaVersion, SCHEMA_VERSION)
            }

            database.withTransaction {
                database.clearAllTables()
                payload.categories.forEach { database.categoryDao().insert(it) }
                payload.accounts.forEach { database.accountDao().insert(it) }
                payload.assets.forEach { database.assetDao().insert(it) }
                payload.liabilities.forEach { database.liabilityDao().insert(it) }
                payload.transactions.forEach { database.transactionDao().insert(it) }
                payload.ledgerEntries.forEach { database.ledgerEntryDao().insert(it) }
                payload.merchantRules.forEach { database.merchantRuleDao().insert(it) }
                payload.budgets.forEach { database.budgetDao().upsert(it) }
                payload.goals.forEach { database.goalDao().insert(it) }
                payload.financialProfile?.let { database.financialProfileDao().upsert(it) }
            }
            RestoreResult.Success
        } catch (e: Exception) {
            RestoreResult.Failure(e.message ?: "Unknown restore error")
        }
    }

    private fun buildEncryptedFile(file: File): EncryptedFile {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedFile.Builder(context, file, masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build()
    }

    companion object {
        const val SCHEMA_VERSION = 1
    }
}

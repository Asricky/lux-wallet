package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Double-entry-style ledger line. A single [TransactionEntity] produces one entry per affected
 * account so internal transfers naturally net out of income/expense totals (PRD §11).
 */
@Serializable
@Entity(
    tableName = "ledger_entries",
    indices = [Index(value = ["accountId"]), Index(value = ["transactionId"])]
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val accountId: Long,
    val deltaAmount: Long,
    val createdAt: Long
)

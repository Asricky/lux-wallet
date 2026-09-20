package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Durable receipt: new transactions only, committed atomically with the ledger. */
@Entity(tableName = "transaction_confirmations")
data class TransactionConfirmationEntity(@PrimaryKey val transactionId: Long, val dueAt: Long, val handled: Boolean = false)

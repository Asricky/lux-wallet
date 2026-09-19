package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.luxwallet.app.core.model.ReviewReason
import com.luxwallet.app.core.model.ReviewStatus
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.model.TransactionType
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["transactionTime"]),
        Index(value = ["reviewStatus"]),
        Index(value = ["sourceAccountId"]),
        Index(value = ["categoryId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val direction: TransactionDirection,
    val amount: Long,
    val currency: String = "IDR",
    val sourceAccountId: Long?,
    val destinationAccountId: Long? = null,
    val merchantName: String? = null,
    val counterpartyName: String? = null,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val transactionTime: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val confidenceScore: Double,
    val reviewStatus: ReviewStatus,
    val reviewReason: ReviewReason? = null,
    val referenceNumber: String? = null,
    val note: String? = null,
    val isInternalTransfer: Boolean = false,
    val isManual: Boolean = false,
    val isExcludedFromCashflow: Boolean = false,
    val reportedBalance: Long? = null,
    /** Observation ids folded into this logical transaction (PRD §9/§17: many-to-one). Comma-joined. */
    val sourceObservationIds: String? = null,
    /** Preserved so a later-arriving opposite leg can still score a destination-hint match bonus regardless of arrival order. */
    val destinationProviderLabelHint: String? = null,
    val destinationOwnerNameHint: String? = null
)

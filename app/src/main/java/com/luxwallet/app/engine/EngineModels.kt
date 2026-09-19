package com.luxwallet.app.engine

import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.TransactionDirection

/**
 * Minimal, engine-facing view of a transaction/observation used by [DeduplicationEngine] and
 * [MatchingEngine]. Deliberately decoupled from Room entities so the engines stay pure Kotlin
 * and unit-testable without an Android runtime or database.
 */
data class LedgerCandidate(
    val id: Long,
    val sourceApp: SourceApp,
    val accountId: Long?,
    val direction: TransactionDirection,
    val amount: Long,
    val merchantOrCounterparty: String? = null,
    val referenceNumber: String? = null,
    val transactionTime: Long,
    val notificationPostedAt: Long,
    val rawPayloadHash: String? = null,
    /** Provider label named explicitly in the notification text, e.g. "ShopeePay"/"GOPAY" (PRD §21 "destination provider explicitly named"). */
    val destinationProviderLabelHint: String? = null,
    /** Owner name named explicitly alongside the destination, e.g. "LUKAS RICKY K." (PRD §12/§21). */
    val destinationOwnerNameHint: String? = null,
    val isInternalTransfer: Boolean = false
)

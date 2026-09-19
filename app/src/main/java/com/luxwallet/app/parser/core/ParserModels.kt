package com.luxwallet.app.parser.core

import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.model.TransactionType

/**
 * Pure input to a [NotificationParser] — deliberately independent of the Room entity so parsers
 * stay plain-Kotlin and unit-testable on the JVM without an Android runtime.
 */
data class NotificationInput(
    val sourceApp: SourceApp,
    val title: String,
    val text: String,
    val bigText: String? = null,
    val subText: String? = null,
    val textLines: List<String> = emptyList(),
    val postedAt: Long
) {
    /** All text fields concatenated, since different OEMs/app versions populate different fields. */
    val combinedText: String by lazy {
        listOfNotNull(title, bigText?.takeIf { it.isNotBlank() } ?: text, subText, textLines.joinToString(" "))
            .filter { it.isNotBlank() }.distinct()
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

/**
 * Normalized, evidence-only extraction from one notification. Never contains invented data:
 * absent fields stay null and drive the confidence score down / Needs Review classification.
 */
data class TransactionCandidate(
    val sourceApp: SourceApp,
    val direction: TransactionDirection,
    val amount: Long,
    val currency: String = "IDR",
    val type: TransactionType,
    val merchantName: String? = null,
    val counterpartyName: String? = null,
    val destinationProviderHint: AccountHint? = null,
    val explicitCategory: String? = null,
    val reportedBalance: Long? = null,
    val referenceNumber: String? = null,
    val transactionTime: Long,
    val confidenceScore: Double,
    val notes: String? = null
)

/** A hint about a counterparty account extracted from notification text, e.g. "GOPAY LUKAS RICKY K." */
data class AccountHint(
    val providerLabel: String,
    val ownerNameRaw: String?
)

sealed class ParseResult {
    data class Parsed(val candidate: TransactionCandidate) : ParseResult()

    /** Notification from a supported app but not a financial transaction event (e.g. promo/marketing). */
    object NotFinancial : ParseResult()

    /** Looked financial but a required field (amount) could not be extracted safely. */
    data class Failed(val reason: String) : ParseResult()
}

interface NotificationParser {
    val sourceApp: SourceApp
    fun canParse(input: NotificationInput): Boolean
    fun parse(input: NotificationInput): ParseResult
}

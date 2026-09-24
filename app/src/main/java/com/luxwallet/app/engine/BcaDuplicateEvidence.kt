package com.luxwallet.app.engine

import com.luxwallet.app.core.database.entity.*
import com.luxwallet.app.core.model.*
import com.luxwallet.app.parser.core.TransactionCandidate
import java.util.Locale
import kotlin.math.abs

object BcaDuplicateEvidence {
    private val packages = setOf("com.bca", "com.bca.android", "com.bca.mybca", "com.bca.mybca.omni.android")
    fun reference(text: String): String? = Regex("""(?i)(?:no\.?\s*(?:ref(?:erensi)?|referen(?:ce)?)|ref(?:erensi|erence)?|nomor referensi)\s*[:#]?\s*([A-Z0-9-]{5,})""").find(text)?.groupValues?.get(1)?.uppercase(Locale.ROOT)
    private fun normalized(value: String?) = value?.lowercase(Locale.ROOT)?.replace(Regex("""[^a-z0-9]"""), "")?.takeIf { it.length >= 5 }
    private fun kind(type: TransactionType) = when(type) {
        TransactionType.QRIS_PAYMENT, TransactionType.MARKETPLACE_PAYMENT, TransactionType.EXPENSE -> "purchase"
        TransactionType.EXTERNAL_TRANSFER, TransactionType.EWALLET_TOPUP -> "transfer"
        TransactionType.INCOME, TransactionType.INTEREST, TransactionType.CASHBACK, TransactionType.REFUND -> "income"
        else -> type.name
    }
    fun compare(old: NotificationObservationEntity, incoming: NotificationObservationEntity, tx: TransactionEntity,
                candidate: TransactionCandidate, accountId: Long): NotificationIdentityMatch? {
        if (old.packageName == incoming.packageName || old.packageName !in packages || incoming.packageName !in packages ||
            old.sourceApp != SourceApp.MYBCA || incoming.sourceApp != SourceApp.MYBCA || tx.isManual || tx.isInternalTransfer ||
            tx.sourceAccountId != accountId || tx.amount != candidate.amount || tx.direction != candidate.direction ||
            kind(tx.type) != kind(candidate.type) || abs(old.postedAt - incoming.postedAt) > 90_000) return null
        val oldRef = tx.referenceNumber ?: reference(old.bigText ?: old.text)
        val newRef = candidate.referenceNumber ?: reference(incoming.bigText ?: incoming.text)
        if (oldRef != null && newRef != null) return if (oldRef == newRef) NotificationIdentityMatch.SAME_EVENT else null
        val oldParty = normalized(tx.merchantName ?: tx.counterpartyName)
        val newParty = normalized(candidate.merchantName ?: candidate.counterpartyName)
        if (oldParty != null && newParty != null && oldParty != newParty) return null
        val unmasked = !(old.bigText ?: old.text).contains('*') && !(incoming.bigText ?: incoming.text).contains('*')
        if (oldParty != null && oldParty == newParty && unmasked && old.eventTime != null && old.eventTime == incoming.eventTime && abs(old.postedAt - incoming.postedAt) <= 10_000)
            return NotificationIdentityMatch.SAME_EVENT
        if (NotificationIdentity.hash(old) == NotificationIdentity.hash(incoming) && old.eventTime != null &&
            old.eventTime == incoming.eventTime) return NotificationIdentityMatch.SAME_EVENT
        return if (abs(old.postedAt - incoming.postedAt) <= 30_000) NotificationIdentityMatch.POSSIBLE_DUPLICATE else null
    }
}

package com.luxwallet.app.engine

import com.luxwallet.app.core.database.entity.NotificationObservationEntity
import com.luxwallet.app.core.model.SourceApp
import java.security.MessageDigest
import java.util.Locale

enum class NotificationIdentityMatch { SAME_EVENT, POSSIBLE_DUPLICATE }

object NotificationIdentity {
    const val REVIEW_WINDOW_MS = 30_000L
    fun contentHash(title: String, text: String, bigText: String?, lines: String?): String {
        val body = bigText?.takeIf { it.isNotBlank() } ?: text.takeIf { it.isNotBlank() } ?: lines.orEmpty()
        val normalized = (body.ifBlank { title }).lowercase(Locale.ROOT).trim().replace(Regex("\\s+"), " ")
        return MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray()).joinToString("") { "%02x".format(it) }
    }
    fun hash(observation: NotificationObservationEntity) = observation.contentHash
        ?: contentHash(observation.title, observation.text, observation.bigText, observation.textLines)

    fun compare(a: NotificationObservationEntity, b: NotificationObservationEntity): NotificationIdentityMatch? {
        if (a.sourceApp != b.sourceApp || a.packageName != b.packageName) return null
        if (a.rawPayloadHash == b.rawPayloadHash) return NotificationIdentityMatch.SAME_EVENT
        val sameKey = !a.notificationKey.isNullOrBlank() && a.notificationKey == b.notificationKey
        if (sameKey && a.eventTime != null && a.eventTime == b.eventTime) return NotificationIdentityMatch.SAME_EVENT
        if (hash(a) != hash(b)) return null
        if (sameKey && a.postedAt == b.postedAt) return NotificationIdentityMatch.SAME_EVENT
        // An identical myBCA financial summary arriving twice is held for review, not discarded.
        if (a.sourceApp == SourceApp.MYBCA && kotlin.math.abs(a.postedAt - b.postedAt) <= REVIEW_WINDOW_MS) {
            return NotificationIdentityMatch.POSSIBLE_DUPLICATE
        }
        return null
    }
}

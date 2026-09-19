package com.luxwallet.app.engine

import com.luxwallet.app.core.model.TransactionDirection
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class MatchResult(
    val match: LedgerCandidate,
    val confidence: Double,
    val autoMatch: Boolean,
    val needsReviewIndicator: Boolean
)

/**
 * Looks for a second observation that, combined with a given one, represents a single internal
 * transfer between two accounts the user owns (PRD §21).
 *
 * Confidence thresholds (PRD §21, kept as named constants so they stay configurable in code):
 *  - >= [AUTO_MATCH_THRESHOLD]      -> auto-match, transaction becomes INTERNAL_TRANSFER and CONFIRMED
 *  - [REVIEW_MATCH_THRESHOLD] – <[AUTO_MATCH_THRESHOLD] -> matched but flagged for user confirmation
 *  - below [REVIEW_MATCH_THRESHOLD] -> not matched, candidate stays Needs Review on its own
 */
object MatchingEngine {
    const val AUTO_MATCH_THRESHOLD = 0.95
    const val REVIEW_MATCH_THRESHOLD = 0.75
    val DEFAULT_TIME_WINDOW_MS: Long = TimeUnit.MINUTES.toMillis(30)

    fun findBestMatch(
        candidate: LedgerCandidate,
        pool: List<LedgerCandidate>,
        isAccountOwnedByUser: (accountId: Long?) -> Boolean,
        accountProviderLabel: (accountId: Long?) -> String?,
        accountOwnerName: (accountId: Long?) -> String?,
        timeWindowMs: Long = DEFAULT_TIME_WINDOW_MS
    ): MatchResult? {
        val opposite = when (candidate.direction) {
            TransactionDirection.OUT -> TransactionDirection.IN
            TransactionDirection.IN -> TransactionDirection.OUT
            TransactionDirection.NONE -> return null
        }

        val eligible = pool.filter { other ->
            other.id != candidate.id &&
                !other.isInternalTransfer &&
                other.direction == opposite &&
                other.amount == candidate.amount &&
                abs(other.transactionTime - candidate.transactionTime) <= timeWindowMs
        }
        if (eligible.isEmpty()) return null

        val best = eligible
            .map { it to score(candidate, it, isAccountOwnedByUser, accountProviderLabel, accountOwnerName) }
            .maxByOrNull { it.second }
            ?: return null

        val (match, confidence) = best
        if (confidence < REVIEW_MATCH_THRESHOLD) return null

        return MatchResult(
            match = match,
            confidence = confidence,
            autoMatch = confidence >= AUTO_MATCH_THRESHOLD,
            needsReviewIndicator = confidence < AUTO_MATCH_THRESHOLD
        )
    }

    private fun score(
        a: LedgerCandidate,
        b: LedgerCandidate,
        isAccountOwnedByUser: (accountId: Long?) -> Boolean,
        accountProviderLabel: (accountId: Long?) -> String?,
        accountOwnerName: (accountId: Long?) -> String?
    ): Double {
        var score = 0.5 // guaranteed by the eligibility filter: equal amount, opposite direction, within window

        val deltaMs = abs(a.transactionTime - b.transactionTime)
        score += when {
            deltaMs <= TimeUnit.MINUTES.toMillis(1) -> 0.20
            deltaMs <= TimeUnit.MINUTES.toMillis(5) -> 0.12
            else -> 0.05
        }

        if (isAccountOwnedByUser(a.accountId) && isAccountOwnedByUser(b.accountId)) {
            score += 0.15
        }

        // A hint on one side always describes the *other* side's real account, never its own.
        val destinationMatches =
            (a.destinationProviderLabelHint != null &&
                accountProviderLabel(b.accountId)?.let { a.destinationProviderLabelHint.contains(it, ignoreCase = true) } == true) ||
            (b.destinationProviderLabelHint != null &&
                accountProviderLabel(a.accountId)?.let { b.destinationProviderLabelHint.contains(it, ignoreCase = true) } == true)
        if (destinationMatches) score += 0.10

        val ownerMatches =
            (a.destinationOwnerNameHint != null &&
                accountOwnerName(b.accountId)?.let { namesLooselyMatch(a.destinationOwnerNameHint, it) } == true) ||
            (b.destinationOwnerNameHint != null &&
                accountOwnerName(a.accountId)?.let { namesLooselyMatch(b.destinationOwnerNameHint, it) } == true)
        if (ownerMatches) score += 0.05

        return score.coerceAtMost(1.0)
    }

    private fun namesLooselyMatch(a: String, b: String): Boolean {
        val normalize = { s: String -> s.uppercase().replace(Regex("[^A-Z ]"), "").trim() }
        return normalize(a) == normalize(b)
    }
}

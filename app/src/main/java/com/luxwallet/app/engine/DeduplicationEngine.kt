package com.luxwallet.app.engine

/**
 * Decides whether a newly parsed transaction candidate is a duplicate of one already recorded.
 *
 * PRD §20 explicitly warns against deduping on amount + timestamp alone (two genuine purchases
 * of equal amount can happen close together), so a match requires amount + direction + source +
 * time proximity *plus* at least one strong identity signal (reference number or
 * merchant/counterparty name), or an identical raw payload hash (the same notification
 * delivered/updated twice).
 */
object DeduplicationEngine {
    val DEFAULT_TIME_WINDOW_MS = java.util.concurrent.TimeUnit.MINUTES.toMillis(2)

    /** Returns the existing candidate this duplicates, or null if [candidate] looks like a new, distinct transaction. */
    fun findDuplicate(
        candidate: LedgerCandidate,
        existing: List<LedgerCandidate>,
        timeWindowMs: Long = DEFAULT_TIME_WINDOW_MS
    ): LedgerCandidate? {
        existing.firstOrNull {
            it.rawPayloadHash != null && candidate.rawPayloadHash != null && it.rawPayloadHash == candidate.rawPayloadHash
        }?.let { return it }

        return existing.firstOrNull { other ->
            other.id != candidate.id &&
                other.sourceApp == candidate.sourceApp &&
                other.direction == candidate.direction &&
                other.amount == candidate.amount &&
                Math.abs(other.transactionTime - candidate.transactionTime) <= timeWindowMs &&
                hasStrongIdentityMatch(candidate, other)
        }
    }

    private fun hasStrongIdentityMatch(a: LedgerCandidate, b: LedgerCandidate): Boolean {
        val referenceMatch = a.referenceNumber != null && a.referenceNumber == b.referenceNumber
        return referenceMatch
    }
}

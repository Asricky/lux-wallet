package com.luxwallet.app.engine

import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.TransactionDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchingEngineTest {

    private val ownedAccounts = setOf(1L, 2L, 3L) // BCA, SeaBank, ShopeePay all owned by user in these fixtures
    private val providerLabels = mapOf(1L to "BCA", 2L to "SeaBank", 3L to "ShopeePay", 4L to "GoPay")
    private val ownerNames = mapOf(4L to "LUKAS RICKY K")

    private val isOwned: (Long?) -> Boolean = { it != null && ownedAccounts.contains(it) || it == 4L }
    private val providerLabel: (Long?) -> String? = { providerLabels[it] }
    private val ownerName: (Long?) -> String? = { ownerNames[it] }

    // Golden samples 2 + 3: BCA IN 329.306 and SeaBank OUT 329.306, no explicit destination named.
    @Test
    fun sample2and3_matchAsInternalTransfer() {
        val bcaIn = LedgerCandidate(
            id = 2, sourceApp = SourceApp.MYBCA, accountId = 1, direction = TransactionDirection.IN,
            amount = 329_306, transactionTime = 1_000_000_000_000, notificationPostedAt = 1_000_000_000_000
        )
        val seabankOut = LedgerCandidate(
            id = 3, sourceApp = SourceApp.SEABANK, accountId = 2, direction = TransactionDirection.OUT,
            amount = 329_306, transactionTime = 1_000_000_030_000, notificationPostedAt = 1_000_000_030_000 // 30s later
        )

        val result = MatchingEngine.findBestMatch(bcaIn, listOf(seabankOut), isOwned, providerLabel, ownerName)

        assertNotNull(result)
        assertEquals(3L, result!!.match.id)
        assertTrue("expected at least review-level confidence, was ${result.confidence}", result.confidence >= MatchingEngine.REVIEW_MATCH_THRESHOLD)
    }

    // Golden samples 4 + 5: ShopeePay IN 10.000 and SeaBank OUT 10.000 "kepada ShopeePay" (explicit destination).
    @Test
    fun sample4and5_matchWithHighConfidenceDueToExplicitDestination() {
        val shopeePayIn = LedgerCandidate(
            id = 4, sourceApp = SourceApp.SHOPEEPAY, accountId = 3, direction = TransactionDirection.IN,
            amount = 10_000, transactionTime = 2_000_000_000_000, notificationPostedAt = 2_000_000_000_000
        )
        val seabankOut = LedgerCandidate(
            id = 5, sourceApp = SourceApp.SEABANK, accountId = 2, direction = TransactionDirection.OUT,
            amount = 10_000, transactionTime = 2_000_000_010_000, notificationPostedAt = 2_000_000_010_000,
            destinationProviderLabelHint = "ShopeePay"
        )

        val result = MatchingEngine.findBestMatch(shopeePayIn, listOf(seabankOut), isOwned, providerLabel, ownerName)

        assertNotNull(result)
        assertEquals(5L, result!!.match.id)
        assertTrue("expected auto-match confidence, was ${result.confidence}", result.autoMatch)
    }

    // Sample 7 style: SeaBank OUT 25.000 "ke GOPAY LUKAS RICKY K." where GoPay account is user-owned with matching owner name.
    @Test
    fun seabankToUserOwnedGoPay_matchesAsInternalTransfer() {
        val gopayIn = LedgerCandidate(
            id = 10, sourceApp = SourceApp.GOPAY, accountId = 4, direction = TransactionDirection.IN,
            amount = 25_000, transactionTime = 3_000_000_000_000, notificationPostedAt = 3_000_000_000_000
        )
        val seabankOut = LedgerCandidate(
            id = 7, sourceApp = SourceApp.SEABANK, accountId = 2, direction = TransactionDirection.OUT,
            amount = 25_000, transactionTime = 3_000_000_020_000, notificationPostedAt = 3_000_000_020_000,
            destinationProviderLabelHint = "GOPAY", destinationOwnerNameHint = "LUKAS RICKY K"
        )

        val result = MatchingEngine.findBestMatch(seabankOut, listOf(gopayIn), isOwned, providerLabel, ownerName)

        assertNotNull(result)
        assertEquals(10L, result!!.match.id)
        assertTrue(result.autoMatch)
    }

    @Test
    fun noOppositeDirectionCandidate_returnsNull() {
        val out = LedgerCandidate(
            id = 1, sourceApp = SourceApp.SEABANK, accountId = 2, direction = TransactionDirection.OUT,
            amount = 50_500, transactionTime = 1_000, notificationPostedAt = 1_000
        )
        assertNull(MatchingEngine.findBestMatch(out, emptyList(), isOwned, providerLabel, ownerName))
    }

    @Test
    fun differentAmounts_doNotMatch() {
        val out = LedgerCandidate(
            id = 1, sourceApp = SourceApp.SEABANK, accountId = 2, direction = TransactionDirection.OUT,
            amount = 50_500, transactionTime = 1_000, notificationPostedAt = 1_000
        )
        val inCandidate = LedgerCandidate(
            id = 2, sourceApp = SourceApp.MYBCA, accountId = 1, direction = TransactionDirection.IN,
            amount = 50_000, transactionTime = 1_500, notificationPostedAt = 1_500
        )
        assertNull(MatchingEngine.findBestMatch(out, listOf(inCandidate), isOwned, providerLabel, ownerName))
    }

    @Test
    fun farApartInTime_doesNotMatch() {
        val out = LedgerCandidate(
            id = 1, sourceApp = SourceApp.SEABANK, accountId = 2, direction = TransactionDirection.OUT,
            amount = 50_500, transactionTime = 0, notificationPostedAt = 0
        )
        val inCandidate = LedgerCandidate(
            id = 2, sourceApp = SourceApp.MYBCA, accountId = 1, direction = TransactionDirection.IN,
            amount = 50_500, transactionTime = java.util.concurrent.TimeUnit.HOURS.toMillis(2), notificationPostedAt = 0
        )
        assertNull(MatchingEngine.findBestMatch(out, listOf(inCandidate), isOwned, providerLabel, ownerName))
    }
}

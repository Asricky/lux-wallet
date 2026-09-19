package com.luxwallet.app.engine

import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.TransactionDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeduplicationEngineTest {

    private fun candidate(
        id: Long,
        amount: Long,
        direction: TransactionDirection = TransactionDirection.OUT,
        merchant: String? = "PopCorn Technology",
        reference: String? = null,
        time: Long = BASE_TIME,
        hash: String? = null
    ) = LedgerCandidate(
        id = id,
        sourceApp = SourceApp.SEABANK,
        accountId = 1,
        direction = direction,
        amount = amount,
        merchantOrCounterparty = merchant,
        referenceNumber = reference,
        transactionTime = time,
        notificationPostedAt = time,
        rawPayloadHash = hash
    )

    @Test
    fun identicalRawHash_isDuplicate() {
        val existing = candidate(1, 50_500, hash = "h1")
        val incoming = candidate(2, 50_500, hash = "h1")
        assertEquals(existing, DeduplicationEngine.findDuplicate(incoming, listOf(existing)))
    }

    @Test
    fun sameAmountSameMerchantCloseInTime_withoutReference_keepsBothPurchases() {
        val existing = candidate(1, 50_500, time = BASE_TIME)
        val incoming = candidate(2, 50_500, time = BASE_TIME + 10_000)
        assertNull(DeduplicationEngine.findDuplicate(incoming, listOf(existing)))
    }

    @Test
    fun sameAmountDifferentMerchantCloseInTime_isNotDuplicate() {
        // Two genuine purchases of equal amount close together must NOT be auto-deduped (PRD §20).
        val existing = candidate(1, 50_500, merchant = "Warung Kopi", time = BASE_TIME)
        val incoming = candidate(2, 50_500, merchant = "PopCorn Technology", time = BASE_TIME + 5_000)
        assertNull(DeduplicationEngine.findDuplicate(incoming, listOf(existing)))
    }

    @Test
    fun sameAmountSameMerchantFarApartInTime_isNotDuplicate() {
        val existing = candidate(1, 50_500, time = BASE_TIME)
        val incoming = candidate(2, 50_500, time = BASE_TIME + java.util.concurrent.TimeUnit.HOURS.toMillis(3))
        assertNull(DeduplicationEngine.findDuplicate(incoming, listOf(existing)))
    }

    @Test
    fun sameReferenceNumber_isDuplicateEvenWithoutMerchant() {
        val existing = candidate(1, 50_500, merchant = null, reference = "REF123", time = BASE_TIME)
        val incoming = candidate(2, 50_500, merchant = null, reference = "REF123", time = BASE_TIME + 1_000)
        assertEquals(existing, DeduplicationEngine.findDuplicate(incoming, listOf(existing)))
    }

    companion object {
        private const val BASE_TIME = 1_726_000_000_000L
    }
}

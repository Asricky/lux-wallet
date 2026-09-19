package com.luxwallet.app.parser

import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.data.NotificationRepository
import com.luxwallet.app.notification.SupportedPackages
import com.luxwallet.app.parser.core.*
import org.junit.Assert.*
import org.junit.Test

class NotificationSafetyTest {
    private val registry = ParserRegistry()
    @Test fun officialSeaBankPackageIsSupportedAndEmailIsIgnored() {
        assertEquals(SourceApp.SEABANK, SupportedPackages.sourceForPackage("id.co.bankbkemobile.digitalbank"))
        assertNull(SupportedPackages.sourceForPackage("com.google.android.gm"))
        assertEquals(SourceApp.MYBCA, SupportedPackages.resolve("example.bank", mapOf("example.bank" to SourceApp.MYBCA)))
    }
    @Test fun declinedAndPromotionalPaymentsDoNotCreateTransactions() {
        listOf("Pembayaran QRIS gagal sebesar Rp50.000", "Promo pembayaran QRIS hingga Rp50.000",
            "Transfer pending sebesar Rp50.000", "Kode verifikasi pembayaran Rp50.000").forEach {
            assertEquals(ParseResult.NotFinancial, registry.parse(NotificationInput(SourceApp.SEABANK, "", it, postedAt = 1L)))
        }
    }
    @Test fun unfamiliarFinancialFormatIsPreservedForReview() {
        assertTrue(registry.parse(NotificationInput(SourceApp.SEABANK, "Transfer diterima",
            "Transfer masuk Rp50.000 dari Budi", postedAt = 1L)) is ParseResult.Failed)
    }
    @Test fun expandedTextAndNotificationIdentityAffectHash() {
        fun hash(key: String, bigText: String) = NotificationRepository.hashPayload("SEABANK", "bank", "Transaksi", "", 1L, key, bigText)
        assertNotEquals(hash("a", "Rp10.000"), hash("a", "Rp20.000"))
        assertNotEquals(hash("a", "Rp10.000"), hash("b", "Rp10.000"))
        assertEquals(hash("a", "Rp10.000"), hash("a", "Rp10.000"))
    }
    @Test fun fullTextTakesPrecedenceOverTruncatedPreview() {
        val result = registry.parse(NotificationInput(SourceApp.SEABANK, "Pembayaran QRIS berhasil",
            "Pembayaran QRIS untuk PopCorn Technology sebesar 50...",
            bigText = "Pembayaran QRIS untuk PopCorn Technology sebesar 50.500 telah berhasil.", postedAt = 1L)) as ParseResult.Parsed
        assertEquals(50500L, result.candidate.amount)
    }
    @Test fun amountNeverOverflowsOrLosesIntegerPrecision() {
        assertEquals(9007199254740993L, AmountParser.normalizeOrNull("9007199254740993"))
        assertNull(AmountParser.normalizeOrNull("999999999999999999999999"))
        assertNull(AmountParser.normalizeOrNull("1,50"))
    }
}

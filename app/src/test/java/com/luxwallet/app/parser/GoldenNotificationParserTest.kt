package com.luxwallet.app.parser

import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.model.TransactionType
import com.luxwallet.app.parser.core.NotificationInput
import com.luxwallet.app.parser.core.ParseResult
import com.luxwallet.app.parser.core.ParserRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the 9 golden notification samples in PRD.md §17.
 * These pin per-notification parser output; cross-notification matching (samples 2+3, 4+5)
 * is covered separately in MatchingEngineTest once the two observations are combined.
 */
class GoldenNotificationParserTest {

    private val registry = ParserRegistry()

    private fun parse(sourceApp: SourceApp, title: String, text: String): ParseResult =
        registry.parse(
            NotificationInput(
                sourceApp = sourceApp,
                title = title,
                text = text,
                postedAt = FIXED_TIME
            )
        )

    // Sample 1 — SeaBank payment to Shopee
    @Test
    fun sample1_seabankInstantPaymentToShopee() {
        val result = parse(
            SourceApp.SEABANK,
            "Pembayaran Berhasil",
            "SeaBank Bayar Instan: Pembayaran Shopee kamu sebesar Rp16.900 ... berhasil."
        )
        val candidate = assertParsed(result)
        assertEquals(TransactionDirection.OUT, candidate.direction)
        assertEquals(16_900L, candidate.amount)
        assertEquals(TransactionType.MARKETPLACE_PAYMENT, candidate.type)
        assertEquals("Shopee", candidate.merchantName)
    }

    // Sample 2 — BCA receives transfer from SeaBank
    @Test
    fun sample2_bcaIncomingTransfer() {
        val result = parse(
            SourceApp.MYBCA,
            "Catatan Finansial",
            "Pemasukan sebesar IDR 329,306.00 dari **KAS **CKY K di kategori Transfer Rekening."
        )
        val candidate = assertParsed(result)
        assertEquals(TransactionDirection.IN, candidate.direction)
        assertEquals(329_306L, candidate.amount)
        assertEquals(TransactionType.EXTERNAL_TRANSFER, candidate.type) // provisional pending matching
        assertEquals("Transfer Rekening", candidate.explicitCategory)
        assertNotNull(candidate.counterpartyName)
    }

    // Sample 3 — SeaBank outgoing realtime transfer
    @Test
    fun sample3_seabankRealtimeTransferOut() {
        val result = parse(
            SourceApp.SEABANK,
            "Realtime Transfer",
            "Kamu baru melakukan transfer real-time senilai Rp329.306 ..."
        )
        val candidate = assertParsed(result)
        assertEquals(TransactionDirection.OUT, candidate.direction)
        assertEquals(329_306L, candidate.amount)
        assertEquals(TransactionType.EXTERNAL_TRANSFER, candidate.type) // provisional pending matching
    }

    // Sample 4 — ShopeePay receives balance
    @Test
    fun sample4_shopeePayTopupIn() {
        val result = parse(
            SourceApp.SHOPEEPAY,
            "Isi Saldo Berhasil",
            "Pengisian saldo sebesar Rp10.000 telah ditambahkan ke ShopeePay-mu. Saldo saat ini sebesar Rp25.191."
        )
        val candidate = assertParsed(result)
        assertEquals(TransactionDirection.IN, candidate.direction)
        assertEquals(10_000L, candidate.amount)
        assertEquals(TransactionType.EWALLET_TOPUP, candidate.type)
        assertEquals(25_191L, candidate.reportedBalance)
    }

    // Sample 5 — SeaBank top-up to ShopeePay
    @Test
    fun sample5_seabankTopupToShopeePay() {
        val result = parse(
            SourceApp.SEABANK,
            "Pembayaran Berhasil",
            "Kamu telah melakukan transfer virtual account sebesar Rp10.000 kepada ShopeePay ..."
        )
        val candidate = assertParsed(result)
        assertEquals(TransactionDirection.OUT, candidate.direction)
        assertEquals(10_000L, candidate.amount)
        assertEquals(TransactionType.EWALLET_TOPUP, candidate.type)
        assertEquals("ShopeePay", candidate.destinationProviderHint?.providerLabel)
    }

    // Sample 6 — SeaBank to QRIS
    @Test
    fun sample6_seabankQrisPayment() {
        val result = parse(
            SourceApp.SEABANK,
            "Pembayaran QRIS berhasil",
            "Pembayaran QRIS untuk PopCorn Technology sebesar 50.500 telah berhasil."
        )
        val candidate = assertParsed(result)
        assertEquals(TransactionDirection.OUT, candidate.direction)
        assertEquals(50_500L, candidate.amount)
        assertEquals(TransactionType.QRIS_PAYMENT, candidate.type)
        assertEquals("PopCorn Technology", candidate.merchantName)
    }

    // Sample 7 — SeaBank top-up GoPay
    @Test
    fun sample7_seabankTopupGoPay() {
        val result = parse(
            SourceApp.SEABANK,
            "Topup e-wallet",
            "Kamu berhasil melakukan Top Up e-Wallet senilai Rp25.000 ke GOPAY LUKAS RICKY K."
        )
        val candidate = assertParsed(result)
        assertEquals(TransactionDirection.OUT, candidate.direction)
        assertEquals(25_000L, candidate.amount)
        assertEquals(TransactionType.EWALLET_TOPUP, candidate.type)
        assertEquals("GOPAY", candidate.destinationProviderHint?.providerLabel)
        assertEquals("LUKAS RICKY K", candidate.destinationProviderHint?.ownerNameRaw)
    }

    // Sample 8 — myBCA expense / Belanja Bulanan
    @Test
    fun sample8_bcaExpenseBelanjaBulanan() {
        val result = parse(
            SourceApp.MYBCA,
            "Catatan Finansial",
            "Pengeluaran sebesar IDR 1.00 di kategori Belanja Bulanan."
        )
        val candidate = assertParsed(result)
        assertEquals(TransactionDirection.OUT, candidate.direction)
        assertEquals(1L, candidate.amount)
        assertEquals("Belanja Bulanan", candidate.explicitCategory)
        assertEquals(TransactionType.EXPENSE, candidate.type)
        // Critical: myBCA never names a merchant, and this parser must never invent one.
        assertNull(candidate.merchantName)
    }

    // Sample 9 — myBCA transfer category
    @Test
    fun sample9_bcaTransferCategory() {
        val result = parse(
            SourceApp.MYBCA,
            "Catatan Finansial",
            "Pengeluaran sebesar IDR 1.00 di kategori Transfer Rekening."
        )
        val candidate = assertParsed(result)
        assertEquals(TransactionDirection.OUT, candidate.direction)
        assertEquals(1L, candidate.amount)
        assertEquals(TransactionType.EXTERNAL_TRANSFER, candidate.type) // provisional; needs matching search
    }

    private fun assertParsed(result: ParseResult) = when (result) {
        is ParseResult.Parsed -> result.candidate
        else -> throw AssertionError("Expected Parsed but was $result")
    }.also { assertTrue(it.confidenceScore in 0.0..1.0) }

    companion object {
        private const val FIXED_TIME = 1_726_000_000_000L
    }
}

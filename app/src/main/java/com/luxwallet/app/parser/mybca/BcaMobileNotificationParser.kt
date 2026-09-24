package com.luxwallet.app.parser.mybca

import com.luxwallet.app.core.model.*
import com.luxwallet.app.parser.core.*

/** Conservative fallback: only explicit completed events; unfamiliar wording stays reviewable. */
class BcaMobileNotificationParser : NotificationParser {
    override val sourceApp = SourceApp.MYBCA
    override fun canParse(input: NotificationInput) = Regex("""(?i)(berhasil|sukses|diterima|dikreditkan|didebit)""").containsMatchIn(input.combinedText)
    override fun parse(input: NotificationInput): ParseResult {
        val text = input.combinedText
        if (Regex("(?i)(tidak berhasil|belum berhasil|belum diterima|tidak sukses)").containsMatchIn(text)) return ParseResult.NotFinancial
        val direction = when {
            Regex("""(?i)(transfer masuk|dana diterima|dikreditkan|pemasukan|kredit sebesar)""").containsMatchIn(text) -> TransactionDirection.IN
            Regex("""(?i)(pembayaran|transfer keluar|transfer ke |didebit|pengeluaran|debit sebesar)""").containsMatchIn(text) -> TransactionDirection.OUT
            else -> return ParseResult.Failed("Arah transaksi BCA belum jelas. Tinjau notifikasi asli.")
        }
        val amounts = Regex("""(?i)(?:Rp\.?|IDR)\s*[0-9]+(?:[.,][0-9]+)*""").findAll(text)
            .mapNotNull { AmountParser.normalizeOrNull(it.value) }.distinct().toList()
        if (amounts.size != 1 || amounts.single() <= 0) return ParseResult.Failed("Nominal transaksi BCA belum dapat dipastikan.")
        val party = Regex("""(?i)(?:merchant|penerima|pengirim)\s*:\s*([^;\n]+?)(?=\s+(?:No\.?\s*Ref|Ref(?:erensi)?|Rp|IDR)|;|$)""").find(text)?.groupValues?.get(1)?.trim()
        val type = when { text.contains("QRIS", true) -> TransactionType.QRIS_PAYMENT
            text.contains("transfer", true) -> TransactionType.EXTERNAL_TRANSFER
            direction == TransactionDirection.IN -> TransactionType.INCOME
            else -> TransactionType.EXPENSE }
        return ParseResult.Parsed(TransactionCandidate(sourceApp, direction, amounts.single(), type = type,
            merchantName = if (type == TransactionType.QRIS_PAYMENT) party else null,
            counterpartyName = if (type != TransactionType.QRIS_PAYMENT) party else null,
            referenceNumber = com.luxwallet.app.engine.BcaDuplicateEvidence.reference(text),
            transactionTime = input.postedAt, confidenceScore = 0.7,
            notes = "Format BCA mobile: periksa rekening dan kategori."))
    }
}

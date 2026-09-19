package com.luxwallet.app.parser.gopay

import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.model.TransactionType
import com.luxwallet.app.parser.core.AmountParser
import com.luxwallet.app.parser.core.NotificationInput
import com.luxwallet.app.parser.core.NotificationParser
import com.luxwallet.app.parser.core.ParseResult
import com.luxwallet.app.parser.core.TransactionCandidate

/**
 * Parser architecture for GoPay (PRD §51 lists "GoPay parser architecture" rather than a full
 * calibrated parser, since — unlike myBCA/SeaBank/ShopeePay — the golden dataset (PRD §17)
 * includes no real GoPay notification sample; GoPay only appears as a *destination* mentioned
 * inside a SeaBank notification (sample 7).
 *
 * This implementation recognizes common Indonesian payment-notification verbs and an explicit
 * Rp-prefixed amount, but deliberately caps confidence below the auto-confirm/needs-review
 * threshold so every extracted candidate lands in Needs Review until real GoPay notification
 * text is captured and this parser is calibrated against it (PRD §3.2: never invent data).
 */
class GoPayNotificationParser : NotificationParser {

    override val sourceApp: SourceApp = SourceApp.GOPAY

    override fun canParse(input: NotificationInput): Boolean = true // package allowlist already restricts to GoPay

    override fun parse(input: NotificationInput): ParseResult {
        val text = input.combinedText

        val amount = AmountParser.findFirstCurrencyPrefixedAmount(text)
            ?: return ParseResult.Failed("Could not extract a Rp-prefixed amount from GoPay notification")

        val direction = when {
            INCOMING_VERBS.any { text.contains(it, ignoreCase = true) } -> TransactionDirection.IN
            OUTGOING_VERBS.any { text.contains(it, ignoreCase = true) } -> TransactionDirection.OUT
            else -> return ParseResult.Failed("Could not determine GoPay transaction direction")
        }

        return ParseResult.Parsed(
            TransactionCandidate(
                sourceApp = sourceApp,
                direction = direction,
                amount = amount,
                type = TransactionType.UNKNOWN,
                transactionTime = input.postedAt,
                confidenceScore = UNCALIBRATED_CONFIDENCE,
                notes = "Uncalibrated GoPay parse: no golden sample available yet."
            )
        )
    }

    companion object {
        private val INCOMING_VERBS = listOf("menerima", "diterima", "masuk", "top up berhasil", "topup berhasil")
        private val OUTGOING_VERBS = listOf("pembayaran", "bayar", "transfer", "dikirim", "membayar")
        private const val UNCALIBRATED_CONFIDENCE = 0.5
    }
}

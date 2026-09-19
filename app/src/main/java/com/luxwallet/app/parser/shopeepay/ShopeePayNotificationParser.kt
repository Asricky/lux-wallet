package com.luxwallet.app.parser.shopeepay

import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.model.TransactionType
import com.luxwallet.app.parser.core.AmountParser
import com.luxwallet.app.parser.core.NotificationInput
import com.luxwallet.app.parser.core.NotificationParser
import com.luxwallet.app.parser.core.ParseResult
import com.luxwallet.app.parser.core.TransactionCandidate

/**
 * Parses ShopeePay notifications. Sample 4 of the golden dataset: a balance top-up that also
 * reports the resulting balance, used later for balance reconciliation (PRD §16).
 */
class ShopeePayNotificationParser : NotificationParser {

    override val sourceApp: SourceApp = SourceApp.SHOPEEPAY

    override fun canParse(input: NotificationInput): Boolean {
        val text = input.combinedText
        return text.contains("ditambahkan ke ShopeePay", ignoreCase = true) ||
            (input.title.contains("Isi Saldo", ignoreCase = true) && text.contains("ShopeePay", ignoreCase = true))
    }

    override fun parse(input: NotificationInput): ParseResult {
        val text = input.combinedText

        if (!text.contains("ditambahkan ke ShopeePay", ignoreCase = true)) {
            return ParseResult.NotFinancial
        }

        val amount = AmountParser.findAmountAfterAnyKeyword(text, listOf("sebesar"))
            ?: return ParseResult.Failed("Could not extract amount from ShopeePay topup")

        // Distinct anchor phrase so we don't re-match the topup amount for the balance figure.
        val reportedBalance = AmountParser.findAmountAfterAnyKeyword(text, listOf("saat ini sebesar"))

        return ParseResult.Parsed(
            TransactionCandidate(
                sourceApp = sourceApp,
                direction = TransactionDirection.IN,
                amount = amount,
                type = TransactionType.EWALLET_TOPUP,
                reportedBalance = reportedBalance,
                transactionTime = input.postedAt,
                confidenceScore = 0.9
            )
        )
    }
}

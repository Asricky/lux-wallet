package com.luxwallet.app.parser.mybca

import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.model.TransactionType
import com.luxwallet.app.parser.core.AmountParser
import com.luxwallet.app.parser.core.NotificationInput
import com.luxwallet.app.parser.core.NotificationParser
import com.luxwallet.app.parser.core.ParseResult
import com.luxwallet.app.parser.core.TransactionCandidate

/**
 * Parses myBCA "Catatan Finansial" notifications (samples 2, 8, 9 of the golden dataset).
 *
 * myBCA only ever provides: direction (Pemasukan/Pengeluaran), amount, category, and — for
 * incoming transfers — a masked counterparty name. It never names a merchant for expenses, so
 * this parser must not guess one (PRD §17 sample 8).
 */
class MyBcaNotificationParser : NotificationParser {

    override val sourceApp: SourceApp = SourceApp.MYBCA

    override fun canParse(input: NotificationInput): Boolean {
        val text = input.combinedText
        return input.title.contains("Catatan Finansial", ignoreCase = true) ||
            text.contains("Pemasukan sebesar", ignoreCase = true) ||
            text.contains("Pengeluaran sebesar", ignoreCase = true)
    }

    override fun parse(input: NotificationInput): ParseResult {
        val text = input.combinedText

        val direction = when {
            text.contains("Pemasukan", ignoreCase = true) -> TransactionDirection.IN
            text.contains("Pengeluaran", ignoreCase = true) -> TransactionDirection.OUT
            else -> return ParseResult.NotFinancial
        }

        val amount = AmountParser.findAmountAfterAnyKeyword(text, listOf("sebesar"))
            ?: return ParseResult.Failed("Could not extract amount from myBCA notification")

        val category = CATEGORY_REGEX.find(text)?.groupValues?.get(1)?.trim()

        val isTransferCategory = category?.equals("Transfer Rekening", ignoreCase = true) == true

        val type = when {
            isTransferCategory -> TransactionType.EXTERNAL_TRANSFER // provisional; matching engine may upgrade to INTERNAL_TRANSFER
            direction == TransactionDirection.IN -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        val counterparty = if (direction == TransactionDirection.IN) {
            COUNTERPARTY_REGEX.find(text)?.groupValues?.get(1)
                ?.replace("*", "")
                ?.replace(Regex("\\s+"), " ")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        } else null

        val confidence = if (amount > 0) 0.9 else 0.5

        return ParseResult.Parsed(
            TransactionCandidate(
                sourceApp = sourceApp,
                direction = direction,
                amount = amount,
                type = type,
                referenceNumber = com.luxwallet.app.engine.BcaDuplicateEvidence.reference(text),
                merchantName = null, // myBCA never names a merchant; never invented.
                counterpartyName = counterparty,
                explicitCategory = category,
                transactionTime = input.postedAt,
                confidenceScore = confidence
            )
        )
    }

    companion object {
        private val CATEGORY_REGEX = Regex("""di kategori ([^.]+)\.?""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_REGEX = Regex("""dari (.+?) di kategori""", RegexOption.IGNORE_CASE)
    }
}

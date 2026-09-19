package com.luxwallet.app.engine

/** A single learned or user-created "merchant contains X -> category Y" rule. */
data class MerchantRule(val merchantContains: String, val categoryId: Long, val subcategoryId: Long? = null)

sealed class CategorizationOutcome {
    /** BCA (or another source) told us the category directly; highest priority, never overridden by guesses. */
    data class Explicit(val categoryName: String) : CategorizationOutcome()
    data class RuleMatched(val categoryId: Long, val subcategoryId: Long?) : CategorizationOutcome()
    data class KeywordMatched(val categoryName: String) : CategorizationOutcome()
    object NeedsReview : CategorizationOutcome()
}

/**
 * Assigns a category following the priority order in PRD §23:
 * explicit category from notification -> user rule -> merchant keyword mapping -> Needs Review.
 */
object CategorizationEngine {

    fun categorize(
        explicitCategory: String?,
        merchantName: String?,
        counterpartyName: String?,
        userRules: List<MerchantRule>
    ): CategorizationOutcome {
        if (!explicitCategory.isNullOrBlank()) {
            return CategorizationOutcome.Explicit(explicitCategory.trim())
        }

        val name = merchantName ?: counterpartyName
        if (name.isNullOrBlank()) return CategorizationOutcome.NeedsReview

        userRules.firstOrNull { name.contains(it.merchantContains, ignoreCase = true) }?.let {
            return CategorizationOutcome.RuleMatched(it.categoryId, it.subcategoryId)
        }

        MERCHANT_KEYWORD_MAP.firstOrNull { it.first.containsMatchIn(name) }?.let {
            return CategorizationOutcome.KeywordMatched(it.second)
        }

        return CategorizationOutcome.NeedsReview
    }

    /** Built-in, deterministic keyword classifier (PRD §23 step 4). Extend as new well-known merchants are found. */
    private val MERCHANT_KEYWORD_MAP: List<Pair<Regex, String>> = listOf(
        Regex("shopee|tokopedia|lazada|bukalapak|blibli", RegexOption.IGNORE_CASE) to "Shopping",
        Regex("gojek|gocar|goride|grab|maxim|bluebird", RegexOption.IGNORE_CASE) to "Transportation",
        Regex("gofood|grabfood|shopeefood", RegexOption.IGNORE_CASE) to "Food & Drink",
        Regex("starbucks|kopi|coffee|fore ", RegexOption.IGNORE_CASE) to "Food & Drink",
        Regex("pln|listrik", RegexOption.IGNORE_CASE) to "Bills",
        Regex("pdam|air minum", RegexOption.IGNORE_CASE) to "Bills",
        Regex("indihome|telkomsel|xl axiata|smartfren|wifi|internet", RegexOption.IGNORE_CASE) to "Bills",
        Regex("netflix|spotify|disney|viu|vidio", RegexOption.IGNORE_CASE) to "Entertainment",
        Regex("apotek|rumah sakit|klinik|pharmacy|hospital", RegexOption.IGNORE_CASE) to "Health",
        Regex("traveloka|tiket\\.com|garuda|airasia|hotel", RegexOption.IGNORE_CASE) to "Travel"
    )
}

package com.luxwallet.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategorizationEngineTest {

    @Test
    fun explicitCategoryAlwaysWins() {
        val result = CategorizationEngine.categorize(
            explicitCategory = "Belanja Bulanan",
            merchantName = "Shopee", // would otherwise keyword-match "Shopping"
            counterpartyName = null,
            userRules = emptyList()
        )
        assertEquals(CategorizationOutcome.Explicit("Belanja Bulanan"), result)
    }

    @Test
    fun userRuleWinsOverKeywordMap() {
        val result = CategorizationEngine.categorize(
            explicitCategory = null,
            merchantName = "FORE COFFEE",
            counterpartyName = null,
            userRules = listOf(MerchantRule(merchantContains = "FORE", categoryId = 99, subcategoryId = 5))
        )
        assertEquals(CategorizationOutcome.RuleMatched(99, 5), result)
    }

    @Test
    fun keywordMapMatchesKnownMerchant() {
        val result = CategorizationEngine.categorize(
            explicitCategory = null,
            merchantName = "Shopee",
            counterpartyName = null,
            userRules = emptyList()
        )
        assertEquals(CategorizationOutcome.KeywordMatched("Shopping"), result)
    }

    @Test
    fun unknownMerchantNeedsReview() {
        // PopCorn Technology (golden sample 6) is not a recognized merchant.
        val result = CategorizationEngine.categorize(
            explicitCategory = null,
            merchantName = "PopCorn Technology",
            counterpartyName = null,
            userRules = emptyList()
        )
        assertEquals(CategorizationOutcome.NeedsReview, result)
    }

    @Test
    fun noMerchantOrCategoryNeedsReview() {
        val result = CategorizationEngine.categorize(null, null, null, emptyList())
        assertTrue(result is CategorizationOutcome.NeedsReview)
    }
}

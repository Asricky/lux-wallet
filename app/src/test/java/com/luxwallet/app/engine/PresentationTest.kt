package com.luxwallet.app.engine

import com.luxwallet.app.core.common.*
import com.luxwallet.app.core.ui.component.*
import org.junit.Assert.*
import org.junit.Test

class PresentationTest {
    @Test fun compactAmountsKeepSignsAndUsefulPrecision() {
        listOf(1000L to "+1K", 10000L to "+10K", 100000L to "+100K", 1000000L to "+1M", 1250000L to "+1.25M", -45000L to "−45K", -750000L to "−750K", 3L to "+3", 0L to "0").forEach { (amount, expected) -> assertEquals(expected, compactCashflow(amount)) }
        assertTrue(compactCashflow(Long.MIN_VALUE).startsWith("−"))
    }
    @Test fun moodsRespectAllDailyBudgetBoundaries() {
        listOf(0.0 to LumiMood.HAPPY, 50.0 to LumiMood.HAPPY, 50.1 to LumiMood.CALM, 74.9 to LumiMood.CALM, 75.0 to LumiMood.NERVOUS, 90.0 to LumiMood.NERVOUS, 100.0 to LumiMood.NERVOUS, 100.1 to LumiMood.SAD, 120.0 to LumiMood.SAD, 120.1 to LumiMood.ANGRY).forEach { (percent, mood) -> assertEquals(mood, budgetMood(percent)) }
        assertEquals(LumiMood.PROUD, budgetMood(40.0, completed = true))
        assertEquals(LumiMood.SAD, budgetMood(110.0, completed = true))
    }
    @Test fun reviewAndIncomeRequireActualEvidence() {
        assertEquals(LumiMood.CALM, companionMood(null, false, false, false))
        assertEquals(LumiMood.CURIOUS, companionMood(null, true, false, true))
        assertEquals(LumiMood.SHOCKED, companionMood(null, false, true, false))
        assertEquals(LumiMood.EXCITED, companionMood(null, false, false, true))
    }
    @Test fun greetingAndPrivacyAreCompactAndDoNotAssumeAName() {
        assertEquals("Hi there 👋", greeting("   "))
        assertEquals("Hi, Lucas 👋", greeting(" Lucas "))
        assertEquals("********", visibleAmount(3200000, true))
        assertEquals("Rp3.200.000", visibleAmount(3200000, false))
    }
}

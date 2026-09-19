package com.luxwallet.app.engine

import org.junit.Assert.*
import org.junit.Test

class FinanceCalculatorTest {
    @Test fun respectsPrecedenceParenthesesAndPercent() {
        assertEquals("7", FinanceCalculator.evaluate("1+2×3").toPlainString())
        assertEquals("9", FinanceCalculator.evaluate("(1+2)×3").toPlainString())
        assertEquals("150000", FinanceCalculator.evaluate("1500000×10%").toPlainString())
        assertEquals("-2.5", FinanceCalculator.evaluate("-5÷2").toPlainString())
    }
    @Test fun rejectsDivisionByZeroAndIncompleteExpression() {
        assertTrue(runCatching { FinanceCalculator.evaluate("5÷0") }.isFailure)
        assertTrue(runCatching { FinanceCalculator.evaluate("5+") }.isFailure)
        assertTrue(runCatching { FinanceCalculator.evaluate("(1+2") }.isFailure)
        assertTrue(runCatching { FinanceCalculator.evaluate("NaN") }.isFailure)
    }
    @Test fun zeroReturnOnlyAccumulatesPrincipal() {
        val value = FinanceCalculator.compound(1000000.0, 1500000.0, 0.0, 10)
        assertEquals(181000000.0, value.futureValue, 0.001)
        assertEquals(value.principal, value.futureValue, 0.001)
    }
    @Test fun effectiveAnnualRateAndEndOfMonthContributionAreConsistent() {
        assertEquals(1060000.0, FinanceCalculator.compound(1000000.0, 0.0, 6.0, 1).futureValue, 0.01)
        assertTrue(FinanceCalculator.compound(0.0, 1500000.0, -10.0, 1).futureValue < 18000000.0)
    }
}

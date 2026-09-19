package com.luxwallet.app.parser

import com.luxwallet.app.parser.core.AmountParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountParserTest {

    @Test fun indonesianDotGrouping() {
        assertEquals(16_900L, AmountParser.normalizeOrNull("Rp16.900"))
        assertEquals(329_306L, AmountParser.normalizeOrNull("Rp329.306"))
        assertEquals(10_000L, AmountParser.normalizeOrNull("Rp10.000"))
        assertEquals(50_500L, AmountParser.normalizeOrNull("50.500"))
    }

    @Test fun internationalCommaGroupingWithDecimal() {
        assertEquals(329_306L, AmountParser.normalizeOrNull("IDR 329,306.00"))
        assertEquals(1L, AmountParser.normalizeOrNull("IDR 1.00"))
    }

    @Test fun plainInteger() {
        assertEquals(1L, AmountParser.normalizeOrNull("1"))
        assertEquals(50_000L, AmountParser.normalizeOrNull("50000"))
    }

    @Test fun multiGroupThousands() {
        assertEquals(1_234_567L, AmountParser.normalizeOrNull("Rp1.234.567"))
    }

    @Test fun commaAsDecimalNoDot() {
        assertNull(AmountParser.normalizeOrNull("1234,56")) // fractions require review, never silently round
    }

    @Test fun noDigitsReturnsNull() {
        assertNull(AmountParser.normalizeOrNull("berhasil"))
        assertNull(AmountParser.normalizeOrNull(""))
    }

    @Test fun findAmountAfterKeyword() {
        val text = "Pengisian saldo sebesar Rp10.000 telah ditambahkan ke ShopeePay-mu. Saldo saat ini sebesar Rp25.191."
        assertEquals(10_000L, AmountParser.findAmountAfterAnyKeyword(text, listOf("sebesar")))
        assertEquals(25_191L, AmountParser.findAmountAfterAnyKeyword(text, listOf("saat ini sebesar")))
    }

    @Test fun findFirstCurrencyPrefixedAmount() {
        assertEquals(50_000L, AmountParser.findFirstCurrencyPrefixedAmount("Kamu menerima GoPay Rp50.000 dari Budi"))
        assertNull(AmountParser.findFirstCurrencyPrefixedAmount("Selamat datang di GoPay"))
    }
}

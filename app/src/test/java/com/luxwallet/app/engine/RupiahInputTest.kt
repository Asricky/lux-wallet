package com.luxwallet.app.engine

import androidx.compose.ui.text.AnnotatedString
import com.luxwallet.app.core.ui.component.RupiahTransformation
import com.luxwallet.app.core.ui.component.rupiahDigits
import org.junit.Assert.*
import org.junit.Test

class RupiahInputTest {
    @Test fun groupingPreservesEveryCursorBoundary() {
        listOf("", "3", "100", "1000", "3200000", "999999999999999").forEach { value ->
            val result = RupiahTransformation.filter(AnnotatedString(value))
            (0..value.length).forEach { offset ->
                assertEquals(offset, result.offsetMapping.transformedToOriginal(result.offsetMapping.originalToTransformed(offset)))
            }
            (0..result.text.length).forEach { offset -> assertTrue(result.offsetMapping.transformedToOriginal(offset) in 0..value.length) }
        }
        assertEquals("3.200.000", RupiahTransformation.filter(AnnotatedString("3200000")).text.text)
    }
    @Test fun pasteSupportsIndonesianGroupingButRejectsAmbiguousDecimals() {
        assertEquals("3200000", rupiahDigits("Rp 3.200.000"))
        assertEquals("", rupiahDigits(""))
        assertNull(rupiahDigits("3.00"))
        assertNull(rupiahDigits("-1000"))
        assertNull(rupiahDigits("2,5"))
        assertNull(rupiahDigits("9999999999999999"))
    }
}

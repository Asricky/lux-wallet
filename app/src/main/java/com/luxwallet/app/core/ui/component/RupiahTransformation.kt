package com.luxwallet.app.core.ui.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Keeps the editable value as digits while grouping the display, including cursor offsets. */
object RupiahTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val display = buildString {
            raw.forEachIndexed { i, c ->
                if (i > 0 && (raw.length - i) % 3 == 0) append('.')
                append(c)
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset == 0) return 0
                return (offset + (1 until offset).count { (raw.length - it) % 3 == 0 }).coerceAtMost(display.length)
            }
            override fun transformedToOriginal(offset: Int) = display.take(offset).count { it != '.' }.coerceAtMost(raw.length)
        }
        return TransformedText(AnnotatedString(display), mapping)
    }
}

fun rupiahDigits(value: String): String? {
    val ungrouped = value.trim().replace(Regex("(?i)^(rp\\.?|idr)\\s*"), "").replace(" ", "")
    if ('.' in ungrouped && !Regex("[0-9]{1,3}(\\.[0-9]{3})+").matches(ungrouped)) return null
    val clean = ungrouped.replace(".", "")
    return clean.takeIf { it.length <= 15 && it.all { c -> c in '0'..'9' } }
}

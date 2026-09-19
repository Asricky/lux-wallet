package com.luxwallet.app.parser.core

/**
 * Normalizes Indonesian / BCA-style currency strings into whole-Rupiah [Long] amounts.
 *
 * Handles both locale conventions seen in the wild:
 *  - Indonesian grouping: "Rp16.900", "Rp329.306" (dot = thousands separator)
 *  - International/BCA "Catatan Finansial" style: "IDR 329,306.00" (comma = thousands, dot = decimal)
 *
 * Disambiguation rule: when only one separator kind is present, a trailing group of exactly
 * 3 digits is treated as a thousands separator; a trailing group of 1-2 digits is treated as a
 * decimal fraction. When both separators are present, whichever occurs last in the string is the
 * decimal separator and the other is stripped as a thousands separator.
 */
object AmountParser {

    /** Returns null if [raw] contains no parseable numeric amount. */
    fun normalizeOrNull(raw: String): Long? {
        val stripped = raw
            .trim()
            .replace(Regex("(?i)rp\\.?"), "")
            .replace(Regex("(?i)idr"), "")
            .trim()
        if (stripped.isEmpty() || !stripped.any { it.isDigit() }) return null

        val hasDot = stripped.contains('.')
        val hasComma = stripped.contains(',')

        val normalized: String = when {
            hasDot && hasComma -> {
                val lastDot = stripped.lastIndexOf('.')
                val lastComma = stripped.lastIndexOf(',')
                if (lastDot > lastComma) {
                    stripped.replace(",", "")
                } else {
                    stripped.replace(".", "").replace(',', '.')
                }
            }
            hasDot -> {
                val lastDot = stripped.lastIndexOf('.')
                val fractionLength = stripped.length - lastDot - 1
                if (fractionLength == 3) stripped.replace(".", "") else stripped
            }
            hasComma -> {
                val lastComma = stripped.lastIndexOf(',')
                val fractionLength = stripped.length - lastComma - 1
                if (fractionLength == 3) stripped.replace(",", "") else stripped.replace(',', '.')
            }
            else -> stripped
        }

        return try {
            normalized.toBigDecimal().setScale(0, java.math.RoundingMode.UNNECESSARY).longValueExact()
        } catch (_: NumberFormatException) { null } catch (_: ArithmeticException) { null }
    }

    /**
     * Finds the first amount-shaped token in [text] that follows one of [anchorKeywords]
     * (case-insensitive), optionally prefixed by a currency marker (Rp/IDR). Used by
     * source-specific parsers so each app's own phrasing decides where the amount lives.
     */
    fun findAmountAfterAnyKeyword(text: String, anchorKeywords: List<String>): Long? {
        for (keyword in anchorKeywords) {
            val idx = text.indexOf(keyword, ignoreCase = true)
            if (idx == -1) continue
            val tail = text.substring(idx + keyword.length)
            val match = AMOUNT_TOKEN_REGEX.find(tail) ?: continue
            val value = normalizeOrNull(match.value)
            if (value != null) return value
        }
        return null
    }

    private val AMOUNT_TOKEN_REGEX =
        Regex("""(?:Rp\.?\s?|IDR\s?)?[0-9]{1,3}(?:[.,][0-9]{2,3})*""")

    private val CURRENCY_PREFIXED_REGEX =
        Regex("""(?:Rp\.?\s?|IDR\s?)[0-9]{1,3}(?:[.,][0-9]{2,3})*""", RegexOption.IGNORE_CASE)

    /**
     * Finds the first amount in [text] that is explicitly prefixed by a currency marker
     * (Rp/IDR), without relying on any keyword anchor. Intended as a conservative fallback for
     * sources whose exact phrasing is not yet calibrated against real notification samples.
     */
    fun findFirstCurrencyPrefixedAmount(text: String): Long? {
        val match = CURRENCY_PREFIXED_REGEX.find(text) ?: return null
        return normalizeOrNull(match.value)
    }
}

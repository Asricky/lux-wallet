package com.luxwallet.app.core.common

import java.util.Locale

/** Displays whole-Rupiah [Long] amounts as "RpX.XXX.XXX" (Indonesian dot grouping, PRD §19). */
object AmountFormat {
    fun rupiah(amount: Long): String {
        val sign = if (amount < 0) "-" else ""
        val grouped = String.format(Locale.US, "%,d", Math.abs(amount)).replace(',', '.')
        return "${sign}Rp$grouped"
    }

    fun plain(amount: Long): String = String.format(Locale.US, "%,d", amount).replace(',', '.')
}

package com.luxwallet.app.parser.core

import com.luxwallet.app.core.model.AccountProvider

/** Maps a free-text provider label seen inside a notification (e.g. "GOPAY", "ShopeePay") to an [AccountProvider]. */
object ProviderLabelMatcher {
    fun match(label: String?): AccountProvider? {
        if (label == null) return null
        val normalized = label.trim().lowercase()
        return when {
            normalized.contains("gopay") -> AccountProvider.GOPAY
            normalized.contains("shopeepay") -> AccountProvider.SHOPEEPAY
            normalized.contains("seabank") -> AccountProvider.SEABANK
            normalized.contains("bca") -> AccountProvider.BCA
            normalized.contains("cash") || normalized.contains("tunai") -> AccountProvider.CASH
            else -> null
        }
    }
}

package com.luxwallet.app.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import java.math.BigDecimal
import java.math.RoundingMode

const val HIDDEN_AMOUNT = "********"
val LocalAmountsHidden = staticCompositionLocalOf { true }
fun visibleAmount(amount: Long, hidden: Boolean) = if (hidden) HIDDEN_AMOUNT else AmountFormat.rupiah(amount)
@Composable fun privateRupiah(amount: Long) = visibleAmount(amount, LocalAmountsHidden.current)
fun greeting(name: String) = name.trim().takeIf { it.isNotEmpty() }?.let { "Hi, $it 👋" } ?: "Hi there 👋"
fun compactCashflow(amount: Long): String {
    val value = BigDecimal.valueOf(amount).abs()
    val divisor = when { value >= BigDecimal("1000000000") -> 1000000000L; value >= BigDecimal("1000000") -> 1000000L; value >= BigDecimal("1000") -> 1000L; else -> 1L }
    val suffix = when(divisor) { 1000000000L -> "B"; 1000000L -> "M"; 1000L -> "K"; else -> "" }
    return (if (amount > 0) "+" else if (amount < 0) "−" else "") + value.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.DOWN).stripTrailingZeros().toPlainString() + suffix
}
enum class NotificationOptions(val label: String) {
    RECORDED("Transaksi tercatat"), INCOME("Pemasukan"), EXPENSE("Pengeluaran"), TRANSFER("Transfer"), REVIEW("Perlu ditinjau"), BUDGET("Peringatan budget")
}

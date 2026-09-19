package com.luxwallet.app.engine

import java.math.BigDecimal
import java.math.MathContext

object FinanceCalculator {
    fun evaluate(raw: String): BigDecimal {
        require(raw.length <= 250) { "Perhitungan terlalu panjang" }
        val input = raw.replace(" ", "").replace('×', '*').replace('÷', '/').replace(',', '.').replace('−', '-')
        var cursor = 0
        var depth = 0
        val mc = MathContext.DECIMAL64
        fun peek() = input.getOrNull(cursor)
        lateinit var expression: () -> BigDecimal
        fun primary(): BigDecimal {
            require(++depth <= 32) { "Terlalu banyak tanda kurung" }
            var value = when (peek()) {
                '+' -> { cursor++; primary() }
                '-' -> { cursor++; primary().negate() }
                '(' -> { cursor++; val v = expression(); require(peek() == ')') { "Tutup tanda kurung" }; cursor++; v }
                else -> {
                    val start = cursor
                    while (peek()?.let { it.isDigit() || it == '.' } == true) cursor++
                    require(cursor > start) { "Lengkapi perhitungan" }
                    input.substring(start, cursor).toBigDecimalOrNull() ?: error("Nominal tidak valid")
                }
            }
            while (peek() == '%') { cursor++; value = value.divide(BigDecimal(100), mc) }
            depth--
            return value
        }
        fun term(): BigDecimal {
            var result = primary()
            while (peek() == '*' || peek() == '/') {
                val operation = input[cursor++]
                val next = primary()
                result = if (operation == '*') result.multiply(next, mc) else {
                    require(next.compareTo(BigDecimal.ZERO) != 0) { "Tidak bisa dibagi nol" }
                    result.divide(next, mc)
                }
            }
            return result
        }
        expression = {
            var result = term()
            while (peek() == '+' || peek() == '-') {
                val operation = input[cursor++]
                result = if (operation == '+') result.add(term(), mc) else result.subtract(term(), mc)
            }
            result
        }
        val result = expression()
        require(cursor == input.length) { "Periksa operator perhitungan" }
        return result.stripTrailingZeros()
    }
    data class Projection(val principal: Double, val futureValue: Double)
    fun compound(initial: Double, monthly: Double, annualPercent: Double, years: Int): Projection {
        require(initial.isFinite() && monthly.isFinite() && annualPercent.isFinite())
        require(initial in 0.0..1e15 && monthly in 0.0..1e12 && annualPercent in -99.0..100.0 && years in 1..60)
        val monthlyRate = Math.pow(1 + annualPercent / 100, 1.0 / 12) - 1
        var value = initial
        repeat(years * 12) { value = value * (1 + monthlyRate) + monthly }
        return Projection(initial + monthly * years * 12, value)
    }
}

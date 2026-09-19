package com.luxwallet.app.core.common

import java.time.DayOfWeek
import java.time.LocalDate

data class TransportPlan(val dailyAmount: Long = 6000, val weekdaysOnly: Boolean = true) {
    fun daysInMonth(today: LocalDate): Int = (1..today.lengthOfMonth()).count {
        !weekdaysOnly || today.withDayOfMonth(it).dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }
    fun monthlyReserve(today: LocalDate) = dailyAmount * daysInMonth(today)
    companion object { const val CATEGORY = "Transportasi rutin" }
}

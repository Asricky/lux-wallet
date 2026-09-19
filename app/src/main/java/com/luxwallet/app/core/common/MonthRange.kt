package com.luxwallet.app.core.common

import java.time.YearMonth
import java.time.ZoneId

/** [startInclusiveMillis, endExclusiveMillis) epoch-millis bounds for a calendar month in the device's default zone. */
data class MonthRange(val yearMonth: YearMonth, val startInclusiveMillis: Long, val endExclusiveMillis: Long) {
    companion object {
        fun of(yearMonth: YearMonth, zone: ZoneId = ZoneId.systemDefault()): MonthRange {
            val start = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val end = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            return MonthRange(yearMonth, start, end)
        }

        fun currentMonth(zone: ZoneId = ZoneId.systemDefault()): MonthRange = of(YearMonth.now(zone), zone)
    }
}

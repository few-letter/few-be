package com.few.generator.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object DateTimeUtils {
    fun createDayRange(date: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        val start = date.atStartOfDay()
        val end = date.atTime(LocalTime.MAX)
        return start to end
    }

    fun getTodayRange(): Pair<LocalDateTime, LocalDateTime> = createDayRange(LocalDate.now())
}
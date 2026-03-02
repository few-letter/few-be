package com.few.generator.support.common

import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate

@Component
class NyseMarketCalendar {
    fun isTradingDay(date: LocalDate): Boolean {
        if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
            return false
        }
        return date !in NYSE_HOLIDAYS
    }

    companion object {
        private val NYSE_HOLIDAYS =
            setOf(
                // 2025
                LocalDate.of(2025, 1, 1),   // New Year's Day
                LocalDate.of(2025, 1, 20),  // MLK Jr. Day
                LocalDate.of(2025, 2, 17),  // Presidents' Day
                LocalDate.of(2025, 4, 18),  // Good Friday
                LocalDate.of(2025, 5, 26),  // Memorial Day
                LocalDate.of(2025, 6, 19),  // Juneteenth
                LocalDate.of(2025, 7, 4),   // Independence Day
                LocalDate.of(2025, 9, 1),   // Labor Day
                LocalDate.of(2025, 11, 27), // Thanksgiving Day
                LocalDate.of(2025, 12, 25), // Christmas Day
                // 2026
                LocalDate.of(2026, 1, 1),   // New Year's Day
                LocalDate.of(2026, 1, 19),  // MLK Jr. Day
                LocalDate.of(2026, 2, 16),  // Presidents' Day
                LocalDate.of(2026, 4, 3),   // Good Friday
                LocalDate.of(2026, 5, 25),  // Memorial Day
                LocalDate.of(2026, 6, 19),  // Juneteenth
                LocalDate.of(2026, 7, 3),   // Independence Day (observed, 7/4 is Saturday)
                LocalDate.of(2026, 9, 7),   // Labor Day
                LocalDate.of(2026, 11, 26), // Thanksgiving Day
                LocalDate.of(2026, 12, 25), // Christmas Day
            )
    }
}

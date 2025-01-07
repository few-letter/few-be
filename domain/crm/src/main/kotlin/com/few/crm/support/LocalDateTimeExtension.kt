package com.few.crm.support

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

fun LocalDateTimeExtension.parseEventTime(date: String): LocalDateTime = LocalDateTime.parse(date, LocalDateTimeExtension.timeFormatter)

fun LocalDateTimeExtension.parseExpiredTime(date: String): LocalDateTime = LocalDateTime.parse(date, LocalDateTimeExtension.timeFormatter)

fun LocalDateTime.toScheduleTime(): Instant =
    Instant.ofEpochSecond(
        this.toEpochSecond(
            ZoneId
                .systemDefault()
                .rules
                .getOffset(Instant.now()),
        ),
    )

class LocalDateTimeExtension {
    companion object {
        val timeFormatter: DateTimeFormatter =
            DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .optionalEnd()
                .toFormatter()
    }
}
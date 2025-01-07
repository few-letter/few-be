package com.few.crm.support

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun LocalDateTimeExtension.parseEventTime(date: String): LocalDateTime =
    LocalDateTime.parse(date, LocalDateTimeExtension.eventTimeFormatter)

fun LocalDateTimeExtension.parseExpiredTime(date: String): LocalDateTime =
    LocalDateTime.parse(date, LocalDateTimeExtension.expiredTimeFormatter)

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
        val eventTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS")
        val expiredTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }
}
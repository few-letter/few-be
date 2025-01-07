package com.few.crm.support

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun LocalDateTimeExtension.parse(date: String): LocalDateTime = LocalDateTime.parse(date, LocalDateTimeExtension.dateTimeFormatter)

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
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    }
}
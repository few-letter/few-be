package com.few.generator.domain.vo

import java.time.LocalDateTime

data class DateTimeRange(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
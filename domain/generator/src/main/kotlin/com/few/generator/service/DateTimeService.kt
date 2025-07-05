package com.few.generator.service

import com.few.generator.domain.vo.DateTimeRange
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DateTimeService {
    fun getTodayTimeRange(): DateTimeRange {
        val start =
            LocalDateTime
                .now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
        val end =
            LocalDateTime
                .now()
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
        return DateTimeRange(startTime = start, endTime = end)
    }
}
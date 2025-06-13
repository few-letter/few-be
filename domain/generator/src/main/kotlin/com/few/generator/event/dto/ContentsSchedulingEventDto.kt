package com.few.generator.event.dto

import java.time.LocalDateTime

data class ContentsSchedulingEventDto(
    val isSuccess: Boolean,
    val startTime: LocalDateTime,
    val totalTime: String,
    val message: String,
    val result: String,
)
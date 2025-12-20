package com.few.generator.event

import java.time.LocalDateTime

data class ContentsSchedulingEvent(
    val title: String,
    val isSuccess: Boolean,
    val startTime: LocalDateTime,
    val totalTime: String,
    val message: String,
    val result: String,
)
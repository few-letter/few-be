package com.few.generator.event.dto

import java.time.LocalDateTime

data class ContentsSchedulingEventDto(
    val isSuccess: Boolean,
    val startTime: LocalDateTime,
    val timeOfCreatingRawContents: String,
    val timeOfCreatingProvisioning: String,
    val timeOfCreatingGens: String,
    val total: String,
    val countByCategory: String,
)
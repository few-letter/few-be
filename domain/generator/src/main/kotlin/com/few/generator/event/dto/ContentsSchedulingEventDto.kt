package com.few.generator.event.dto

import java.time.LocalDateTime

data class ContentsSchedulingEventDto(
    val isSuccess: Boolean,
    val startTime: LocalDateTime,
    val timeOfCreatingRawContents: String,
    val timeOfCreatingProvisionings: String,
    val timeOfCreatingGens: String,
    val totalTime: String,
    val countByCategory: String,
)
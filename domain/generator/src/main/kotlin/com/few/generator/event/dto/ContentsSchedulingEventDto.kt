package com.few.generator.event.dto

data class ContentsSchedulingEventDto(
    val isSuccess: Boolean,
    val timeOfCreatingRawContents: String,
    val timeOfCreatingProvisioning: String,
    val timeOfCreatingGens: String,
    val total: String,
)
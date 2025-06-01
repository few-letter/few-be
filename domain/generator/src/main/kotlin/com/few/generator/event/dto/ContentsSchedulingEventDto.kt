package com.few.generator.event.dto

data class ContentsSchedulingEventDto(
    val timeOfCreatingRawContents: String,
    val timeOfCreatingProvisioning: String,
    val timeOfCreatingGens: String,
    val total: String,
)
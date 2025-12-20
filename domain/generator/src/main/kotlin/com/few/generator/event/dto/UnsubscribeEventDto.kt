package com.few.generator.event.dto

import com.few.common.domain.ContentsType
import java.time.LocalDateTime

data class UnsubscribeEventDto(
    val email: String,
    val categories: String,
    val contentsType: ContentsType,
    val unsubscribedAt: LocalDateTime,
)
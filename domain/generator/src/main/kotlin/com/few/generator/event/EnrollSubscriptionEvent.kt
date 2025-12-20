package com.few.generator.event

import com.few.common.domain.ContentsType
import java.time.LocalDateTime

data class EnrollSubscriptionEvent(
    val email: String,
    val categories: String,
    val contentsType: ContentsType,
    val enrolledAt: LocalDateTime,
)
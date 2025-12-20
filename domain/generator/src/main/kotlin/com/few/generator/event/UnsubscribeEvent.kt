package com.few.generator.event

import com.few.common.domain.ContentsType
import java.time.LocalDateTime

data class UnsubscribeEvent(
    val email: String,
    val categories: String,
    val contentsType: ContentsType,
    val unsubscribedAt: LocalDateTime,
)
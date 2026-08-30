package com.few.generator.event

import com.few.common.domain.ContentsType
import com.few.common.domain.Region
import java.time.LocalDateTime

data class TriggerContentsPublishSkillsEvent(
    val title: String,
    val startTime: LocalDateTime,
    val region: Region? = null,
    val contentsType: ContentsType,
)
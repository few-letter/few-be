package com.few.generator.event

import com.few.common.domain.ContentsType
import com.few.common.domain.Region
import java.time.LocalDateTime

data class TriggerContentsPublishSkillsEvent(
    val eventTitle: String,
    val startTime: LocalDateTime,
    val newsContentsEvent: NewsContentsEvent? = null,
    val contentsType: ContentsType,
)

data class NewsContentsEvent(
    val region: Region,
)
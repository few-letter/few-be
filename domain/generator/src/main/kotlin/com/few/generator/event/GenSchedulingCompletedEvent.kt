package com.few.generator.event

import com.few.common.domain.Region

data class GenSchedulingCompletedEvent(
    val region: Region,
)
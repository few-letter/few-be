package com.few.generator.usecase.out

import com.few.common.domain.ContentsType

data class CheckPublishableContentUseCaseOut(
    val hasPublishableContent: Boolean,
    val count: Int,
    val contentsTypes: List<ContentsType>?,
)
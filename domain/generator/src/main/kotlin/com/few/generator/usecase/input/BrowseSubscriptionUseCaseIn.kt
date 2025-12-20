package com.few.generator.usecase.input

import com.few.common.domain.ContentsType

data class BrowseSubscriptionUseCaseIn(
    val email: String,
    val contentsType: ContentsType = ContentsType.LOCAL_NEWS,
)
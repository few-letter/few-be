package com.few.generator.usecase.input

import com.few.common.domain.ContentsType

data class EnrollSubscriptionUseCaseIn(
    val email: String,
    val categoryCodes: List<Int>,
    val contentsType: ContentsType = ContentsType.NAVER_LOCAL_NEWS,
)
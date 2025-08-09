package com.few.provider.controller.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class EnrollSubscriptionRequest(
    @field:NotNull(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,
    @field:NotEmpty(message = "카테고리는 최소 1개 이상 선택해야 합니다")
    val categoryCodes: List<Int>,
)
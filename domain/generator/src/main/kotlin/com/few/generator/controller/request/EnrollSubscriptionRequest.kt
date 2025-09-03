package com.few.generator.controller.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class EnrollSubscriptionRequest
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    constructor(
        @field:NotNull(message = "이메일은 필수입니다")
        @field:Email(message = "올바른 이메일 형식이 아닙니다")
        @JsonProperty("email")
        val email: String,
        @field:NotEmpty(message = "카테고리는 최소 1개 이상 선택해야 합니다")
        @JsonProperty("categoryCodes")
        val categoryCodes: List<Int>,
    )
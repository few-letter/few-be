package com.few.generator.usecase.input

data class EnrollSubscriptionUseCaseIn(
    val email: String,
    val categoryCodes: List<Int>,
)
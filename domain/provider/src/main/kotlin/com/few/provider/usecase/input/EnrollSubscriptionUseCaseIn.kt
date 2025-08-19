package com.few.provider.usecase.input

data class EnrollSubscriptionUseCaseIn(
    val email: String,
    val categoryCodes: List<Int>,
)
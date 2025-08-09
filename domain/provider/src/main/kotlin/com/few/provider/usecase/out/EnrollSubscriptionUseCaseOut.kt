package com.few.provider.usecase.out

data class EnrollSubscriptionUseCaseOut(
    val existingCategories: List<Int>,
    val newCategories: List<Int>,
)
package com.few.provider.usecase.out

import common.domain.Category

data class EnrollSubscriptionUseCaseOut(
    val existingCategories: List<Category>,
    val newCategories: List<Category>,
)
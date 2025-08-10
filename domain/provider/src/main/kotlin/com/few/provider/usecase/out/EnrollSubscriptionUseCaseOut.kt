package com.few.provider.usecase.out

import common.domain.Category

data class EnrollSubscriptionUseCaseOut(
    val subscribedCategories: Set<Category>,
)
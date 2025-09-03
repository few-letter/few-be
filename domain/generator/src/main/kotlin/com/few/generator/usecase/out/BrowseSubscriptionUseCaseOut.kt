package com.few.generator.usecase.out

import com.few.common.domain.Category

data class BrowseSubscriptionUseCaseOut(
    val subscribedCategories: Set<Category>,
)
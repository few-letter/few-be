package com.few.provider.usecase.out

import com.few.common.domain.Category

data class BrowseSubscriptionUseCaseOut(
    val subscribedCategories: Set<Category>,
)
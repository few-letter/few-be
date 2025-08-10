package com.few.provider.usecase.out

import common.domain.Category

data class BrowseSubscriptionUseCaseOut(
    val subscribedCategories: Set<Category>,
)
package com.few.provider.usecase

import com.few.provider.repository.SubscriptionRepository
import com.few.provider.support.jpa.ProviderTransactional
import com.few.provider.usecase.input.BrowseSubscriptionUseCaseIn
import com.few.provider.usecase.out.EnrollSubscriptionUseCaseOut
import common.domain.Category
import org.springframework.stereotype.Component

@Component
data class BrowseSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
) {
    @ProviderTransactional(readOnly = true)
    fun execute(input: BrowseSubscriptionUseCaseIn): EnrollSubscriptionUseCaseOut {
        val existing = subscriptionRepository.findByEmail(input.email)

        return if (existing != null) {
            val categories =
                existing.categories
                    .split(",")
                    .filter { it.isNotBlank() }
                    .map {
                        Category.from(it.toInt())
                    }.toSet()

            EnrollSubscriptionUseCaseOut(categories)
        } else {
            EnrollSubscriptionUseCaseOut(emptySet())
        }
    }
}
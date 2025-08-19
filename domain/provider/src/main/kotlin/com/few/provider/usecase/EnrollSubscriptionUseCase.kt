package com.few.provider.usecase

import com.few.common.domain.Category
import com.few.provider.domain.Subscription
import com.few.provider.domain.SubscriptionAction
import com.few.provider.domain.SubscriptionHis
import com.few.provider.repository.SubscriptionHisRepository
import com.few.provider.repository.SubscriptionRepository
import com.few.provider.support.jpa.ProviderTransactional
import com.few.provider.usecase.input.EnrollSubscriptionUseCaseIn
import com.few.provider.usecase.out.BrowseSubscriptionUseCaseOut
import org.springframework.stereotype.Component

@Component
data class EnrollSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionHisRepository: SubscriptionHisRepository,
) {
    @ProviderTransactional
    fun execute(input: EnrollSubscriptionUseCaseIn): BrowseSubscriptionUseCaseOut {
        val existing = subscriptionRepository.findByEmail(input.email)

        val joinedCategories = input.categoryCodes.distinct().joinToString(",") { it.toString() }
        val categories = input.categoryCodes.map { Category.from(it) }.toSet()

        subscriptionHisRepository.save(
            SubscriptionHis(
                email = input.email,
                categories = joinedCategories,
                action = SubscriptionAction.ENROLL.code,
            ),
        )

        return if (existing != null) {
            existing.categories = joinedCategories
            subscriptionRepository.save(existing)
            BrowseSubscriptionUseCaseOut(categories)
        } else {
            subscriptionRepository.save(Subscription(email = input.email, categories = joinedCategories))
            BrowseSubscriptionUseCaseOut(categories)
        }
    }
}
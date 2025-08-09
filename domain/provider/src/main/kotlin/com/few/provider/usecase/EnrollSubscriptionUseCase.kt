package com.few.provider.usecase

import com.few.provider.domain.Subscription
import com.few.provider.domain.SubscriptionAction
import com.few.provider.domain.SubscriptionHis
import com.few.provider.repository.SubscriptionHisRepository
import com.few.provider.repository.SubscriptionRepository
import com.few.provider.support.jpa.ProviderTransactional
import com.few.provider.usecase.input.EnrollSubscriptionUseCaseIn
import com.few.provider.usecase.out.EnrollSubscriptionUseCaseOut
import common.domain.Category
import org.springframework.stereotype.Component

@Component
data class EnrollSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionHisRepository: SubscriptionHisRepository,
) {
    @ProviderTransactional
    fun execute(input: EnrollSubscriptionUseCaseIn): EnrollSubscriptionUseCaseOut {
        val categories = input.categoryCodes.map { code -> Category.from(code) }

        val existingCategories = mutableListOf<Category>()
        val newCategories = mutableListOf<Category>()

        categories.forEach { category ->
            val existingSubscription =
                subscriptionRepository.findByEmailAndCategory(input.email, category.code)

            if (existingSubscription != null) {
                existingCategories.add(Category.from(category.code))
            } else {
                subscriptionRepository.save(
                    Subscription(
                        email = input.email,
                        category = category.code,
                    ),
                )

                subscriptionHisRepository.save(
                    SubscriptionHis(
                        email = input.email,
                        category = category.code,
                        action = SubscriptionAction.ENROLL.code,
                    ),
                )

                newCategories.add(Category.from(category.code))
            }
        }

        return EnrollSubscriptionUseCaseOut(
            existingCategories = existingCategories,
            newCategories = newCategories,
        )
    }
}
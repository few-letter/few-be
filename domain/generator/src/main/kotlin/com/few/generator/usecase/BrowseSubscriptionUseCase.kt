package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.generator.repository.SubscriptionRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.input.BrowseSubscriptionUseCaseIn
import com.few.generator.usecase.out.BrowseSubscriptionUseCaseOut
import org.springframework.stereotype.Component

@Component
data class BrowseSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
) {
    @GeneratorTransactional(readOnly = true)
    fun execute(input: BrowseSubscriptionUseCaseIn): BrowseSubscriptionUseCaseOut {
        val existing = subscriptionRepository.findByEmailAndContentsType(input.email, input.contentsType)

        return if (existing != null) {
            val categories =
                existing.categories
                    .split(",")
                    .filter { it.isNotBlank() }
                    .map {
                        Category.from(it.toInt())
                    }.toSet()

            BrowseSubscriptionUseCaseOut(categories)
        } else {
            BrowseSubscriptionUseCaseOut(emptySet())
        }
    }
}
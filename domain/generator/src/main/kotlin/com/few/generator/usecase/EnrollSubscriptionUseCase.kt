package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.generator.domain.Subscription
import com.few.generator.domain.SubscriptionAction
import com.few.generator.domain.SubscriptionHis
import com.few.generator.event.dto.EnrollSubscriptionEventDto
import com.few.generator.repository.SubscriptionHisRepository
import com.few.generator.repository.SubscriptionRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.input.EnrollSubscriptionUseCaseIn
import com.few.generator.usecase.out.BrowseSubscriptionUseCaseOut
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
data class EnrollSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionHisRepository: SubscriptionHisRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @GeneratorTransactional
    fun execute(input: EnrollSubscriptionUseCaseIn): BrowseSubscriptionUseCaseOut {
        val existing = subscriptionRepository.findByEmail(input.email)

        val joinedCategories = input.categoryCodes.distinct().joinToString(",") { it.toString() }
        val categories = input.categoryCodes.map { Category.Companion.from(it) }.toSet()

        subscriptionHisRepository.save(
            SubscriptionHis(
                email = input.email,
                categories = joinedCategories,
                action = SubscriptionAction.ENROLL.code,
            ),
        )

        applicationEventPublisher.publishEvent(
            EnrollSubscriptionEventDto(
                email = input.email,
                categories = categories.joinToString(", ") { it.title },
                enrolledAt = LocalDateTime.now(),
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
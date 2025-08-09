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
        val requestedCategories = input.categoryCodes.map { code -> Category.from(code) }

        // 기존 구독 정보 조회
        val existingSubscription = subscriptionRepository.findByEmail(input.email)

        return if (existingSubscription != null) {
            handleExistingSubscription(existingSubscription, requestedCategories)
        } else {
            handleFirstSubscription(input.email, requestedCategories)
        }
    }

    private fun handleExistingSubscription(
        existingSubscription: Subscription,
        requestedCategories: List<Category>,
    ): EnrollSubscriptionUseCaseOut {
        val existingCategories = mutableListOf<Category>()
        val newCategories = mutableListOf<Category>()

        // 현재 구독 중인 카테고리 파싱
        val currentCategories =
            existingSubscription.categories.split(",").filter { it.isNotBlank() }.map {
                it.toInt()
            }

        requestedCategories.forEach { requestedCategory ->
            if (currentCategories.contains(requestedCategory.code)) {
                existingCategories.add(requestedCategory)
            } else {
                newCategories.add(requestedCategory)
            }
        }

        // 신규 카테고리가 있는 경우 기존 구독 업데이트
        if (newCategories.isNotEmpty()) {
            val updatedCategories =
                (currentCategories + newCategories.map { it.code }).joinToString(",")
            existingSubscription.categories = updatedCategories
            subscriptionRepository.save(existingSubscription)

            // 신규 카테고리들을 SubscriptionHis에 한 번에 저장
            val newCategoryCodes = newCategories.map { it.code }.joinToString(",")
            subscriptionHisRepository.save(
                SubscriptionHis(
                    email = existingSubscription.email,
                    categories = newCategoryCodes,
                    action = SubscriptionAction.ENROLL.code,
                ),
            )
        }

        return EnrollSubscriptionUseCaseOut(
            existingCategories = existingCategories,
            newCategories = newCategories,
        )
    }

    private fun handleFirstSubscription(
        email: String,
        requestedCategories: List<Category>,
    ): EnrollSubscriptionUseCaseOut {
        val categoryCodes = requestedCategories.map { it.code }
        val categoriesString = categoryCodes.joinToString(",")

        // Subscription 저장
        subscriptionRepository.save(
            Subscription(
                email = email,
                categories = categoriesString,
            ),
        )

        // SubscriptionHis에 모든 카테고리를 한 번에 저장
        subscriptionHisRepository.save(
            SubscriptionHis(
                email = email,
                categories = categoriesString,
                action = SubscriptionAction.ENROLL.code,
            ),
        )

        return EnrollSubscriptionUseCaseOut(
            existingCategories = emptyList(),
            newCategories = requestedCategories,
        )
    }
}
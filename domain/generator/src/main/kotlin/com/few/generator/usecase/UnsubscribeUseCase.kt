package com.few.generator.usecase

import com.few.common.exception.BadRequestException
import com.few.generator.domain.SubscriptionAction
import com.few.generator.domain.SubscriptionHis
import com.few.generator.repository.SubscriptionHisRepository
import com.few.generator.repository.SubscriptionRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.input.UnsubscribeUseCaseIn
import org.springframework.stereotype.Component

@Component
data class UnsubscribeUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionHisRepository: SubscriptionHisRepository,
) {
    @GeneratorTransactional
    fun execute(input: UnsubscribeUseCaseIn) {
        val existing =
            subscriptionRepository.findByEmail(input.email)
                ?: throw BadRequestException("구독하지 않은 이메일입니다.")

        subscriptionHisRepository.save(
            SubscriptionHis(
                email = input.email,
                categories = existing.categories,
                action = SubscriptionAction.CANCEL.code,
            ),
        )
        subscriptionRepository.delete(existing)
    }
}
package com.few.provider.usecase

import com.few.common.exception.BadRequestException
import com.few.provider.domain.SubscriptionAction
import com.few.provider.domain.SubscriptionHis
import com.few.provider.repository.SubscriptionHisRepository
import com.few.provider.repository.SubscriptionRepository
import com.few.provider.support.jpa.ProviderTransactional
import com.few.provider.usecase.input.UnsubscribeUseCaseIn
import org.springframework.stereotype.Component

@Component
data class UnsubscribeUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionHisRepository: SubscriptionHisRepository,
) {
    @ProviderTransactional
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
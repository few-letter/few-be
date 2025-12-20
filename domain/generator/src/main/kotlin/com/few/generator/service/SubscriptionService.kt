package com.few.generator.service

import com.few.common.domain.ContentsType
import com.few.generator.repository.SubscriptionRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
) {
    fun findAll(
        contentsType: ContentsType,
        pageable: Pageable,
    ) = subscriptionRepository.findAllByContentsType(contentsType, pageable)
}
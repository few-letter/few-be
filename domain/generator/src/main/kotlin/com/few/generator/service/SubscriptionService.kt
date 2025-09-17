package com.few.generator.service

import com.few.generator.repository.SubscriptionRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
) {
    fun findAll(pageable: Pageable) = subscriptionRepository.findAll(pageable)
}
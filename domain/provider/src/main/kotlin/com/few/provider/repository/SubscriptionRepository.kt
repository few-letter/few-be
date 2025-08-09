package com.few.provider.repository

import com.few.provider.domain.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findByEmailAndCategory(
        email: String,
        categoryCode: Int,
    ): Subscription?
}
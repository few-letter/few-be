package com.few.generator.repository

import com.few.generator.domain.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findByEmail(email: String): Subscription?
}
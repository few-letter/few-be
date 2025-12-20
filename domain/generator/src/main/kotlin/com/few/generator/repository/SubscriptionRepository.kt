package com.few.generator.repository

import com.few.common.domain.ContentsType
import com.few.generator.domain.Subscription
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findByEmailAndContentsType(
        email: String,
        contentsType: ContentsType,
    ): Subscription?

    fun findAllByContentsType(
        contentsType: ContentsType,
        pageable: Pageable,
    ): Page<Subscription>
}
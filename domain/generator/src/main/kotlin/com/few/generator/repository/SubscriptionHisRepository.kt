package com.few.generator.repository

import com.few.generator.domain.SubscriptionHis
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionHisRepository : JpaRepository<SubscriptionHis, Long> {
    override fun <S : SubscriptionHis> save(entity: S): S
}
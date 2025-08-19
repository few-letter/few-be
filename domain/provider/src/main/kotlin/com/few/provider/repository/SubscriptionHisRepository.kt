package com.few.provider.repository

import com.few.provider.domain.SubscriptionHis
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionHisRepository : JpaRepository<SubscriptionHis, Long>
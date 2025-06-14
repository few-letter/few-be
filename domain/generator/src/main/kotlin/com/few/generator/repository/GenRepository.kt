package com.few.generator.repository

import com.few.generator.domain.Gen
import org.springframework.data.jpa.repository.JpaRepository

interface GenRepository : JpaRepository<Gen, Long> {
    fun findByProvisioningContentsId(provisioningContentsId: Long): List<Gen>

    fun existsByProvisioningContentsId(provisioningContentsId: Long): Boolean
}
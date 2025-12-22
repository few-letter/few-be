package com.few.generator.repository

import com.few.generator.domain.ProvisioningContents
import org.springframework.data.jpa.repository.JpaRepository

interface ProvisioningContentsRepository : JpaRepository<ProvisioningContents, Long> {
    override fun <S : ProvisioningContents> saveAll(entities: Iterable<S>): List<S>

    fun findByRawContentsId(rawContentsId: Long): ProvisioningContents?

    fun findAllByIdIn(ids: List<Long>): List<ProvisioningContents>
}
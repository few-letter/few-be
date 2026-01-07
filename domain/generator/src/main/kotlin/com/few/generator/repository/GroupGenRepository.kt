package com.few.generator.repository

import com.few.generator.domain.GroupGen
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface GroupGenRepository : JpaRepository<GroupGen, Long> {
    override fun <S : GroupGen> save(entity: S): S

    fun findAllByCreatedAtBetweenAndRegion(
        start: LocalDateTime,
        end: LocalDateTime,
        region: Int,
    ): List<GroupGen>

    fun findFirstByRegionOrderByCreatedAtDesc(region: Int): GroupGen?
}
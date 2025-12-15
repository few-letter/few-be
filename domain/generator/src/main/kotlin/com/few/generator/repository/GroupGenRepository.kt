package com.few.generator.repository

import com.few.generator.domain.GroupGen
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface GroupGenRepository : JpaRepository<GroupGen, Long> {
    @Cacheable(value = ["groupGenByDateRange"], key = "#start.toString() + '_' + #end.toString()")
    fun findAllByCreatedAtBetween(
        start: LocalDateTime,
        end: LocalDateTime,
    ): List<GroupGen>

    @CacheEvict(value = ["groupGenByDateRange"], allEntries = true)
    override fun <S : GroupGen> save(entity: S): S
}
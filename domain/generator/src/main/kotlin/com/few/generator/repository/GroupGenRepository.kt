package com.few.generator.repository

import com.few.generator.domain.GroupGen
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface GroupGenRepository : JpaRepository<GroupGen, Long> {
    fun findAllByCreatedAtBetween(
        start: LocalDateTime,
        end: LocalDateTime,
    ): List<GroupGen>
}
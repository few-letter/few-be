package com.few.generator.repository

import com.few.generator.domain.GroupGen
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface GroupGenRepository : JpaRepository<GroupGen, Long> {
    fun findAllByCreatedAtBetweenAndCategory(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        category: Int,
    ): List<GroupGen>

    @Query(
        "SELECT * FROM group_gen WHERE category = :category ORDER BY created_at DESC LIMIT :limitSize",
        nativeQuery = true,
    )
    fun findFirstLimitByCategory(
        @Param("category") category: Int,
        @Param("limitSize") limitSize: Int,
    ): List<GroupGen>

    @Query(
        "SELECT * FROM group_gen ORDER BY created_at DESC LIMIT :limitSize",
        nativeQuery = true,
    )
    fun findFirstLimit(
        @Param("limitSize") limitSize: Int,
    ): List<GroupGen>
}
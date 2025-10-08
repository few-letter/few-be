package com.few.generator.repository

import com.few.generator.domain.Gen
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface GenRepository : JpaRepository<Gen, Long> {
    @Query(
        """
        SELECT g.* FROM gen g
        WHERE g.created_at < (
            SELECT created_at FROM gen WHERE id = :targetId
        )
        ORDER BY g.created_at DESC
        LIMIT :limitSize
        """,
        nativeQuery = true,
    )
    fun findNextLimit(
        @Param("targetId") targetId: Long,
        @Param("limitSize") limitSize: Int,
    ): List<Gen>

    @Query(
        "SELECT * FROM gen ORDER BY created_at DESC LIMIT :limitSize",
        nativeQuery = true,
    )
    fun findFirstLimit(
        @Param("limitSize") limitSize: Int,
    ): List<Gen>

    @Query(
        """
        SELECT g.* FROM gen g
        WHERE g.category = :category
        AND g.created_at < (
            SELECT created_at FROM gen WHERE id = :targetId
        )
        AND g.region = :region
        ORDER BY g.created_at DESC
        LIMIT :limitSize
        """,
        nativeQuery = true,
    )
    fun findNextLimitByCategory(
        @Param("targetId") targetId: Long,
        @Param("category") category: Int,
        @Param("limitSize") limitSize: Int,
        @Param("region") region: Int,
    ): List<Gen>

    @Query(
        "SELECT * FROM gen WHERE category = :category and region = :region ORDER BY created_at DESC LIMIT :limitSize",
        nativeQuery = true,
    )
    fun findFirstLimitByCategory(
        @Param("category") category: Int,
        @Param("limitSize") limitSize: Int,
        @Param("region") region: Int,
    ): List<Gen>

    fun findAllByCreatedAtBetweenAndCategoryAndRegion(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        category: Int,
        region: Int,
    ): List<Gen>

    fun findAllByCreatedAtBetweenAndRegion(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        region: Int,
    ): List<Gen>
}
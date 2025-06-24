package com.few.generator.repository

import com.few.generator.domain.Gen
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GenRepository : JpaRepository<Gen, Long> {
    fun findByProvisioningContentsId(provisioningContentsId: Long): List<Gen>

    fun existsByProvisioningContentsId(provisioningContentsId: Long): Boolean

    @Query(
        """
    SELECT * FROM (
        SELECT *, ROW_NUMBER() OVER (ORDER BY created_at DESC) AS rn
        FROM gen
    ) ranked
    WHERE rn > (
        SELECT target_rn FROM (
            SELECT id, ROW_NUMBER() OVER (ORDER BY created_at DESC) AS target_rn
            FROM gen
        ) sub
        WHERE sub.id = :targetId
    )
    ORDER BY rn
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
}
package com.few.generator.repository

import com.few.generator.domain.Gen
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GenRepository : JpaRepository<Gen, Long> {
    fun findByProvisioningContentsId(provisioningContentsId: Long): List<Gen>

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
}
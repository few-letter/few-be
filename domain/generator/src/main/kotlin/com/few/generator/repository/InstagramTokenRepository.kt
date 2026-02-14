package com.few.generator.repository

import com.few.generator.domain.InstagramToken
import org.springframework.data.jpa.repository.JpaRepository

interface InstagramTokenRepository : JpaRepository<InstagramToken, Long> {
    override fun <S : InstagramToken> save(entity: S): S

    fun findTopByOrderByCreatedAtDesc(): InstagramToken?
}
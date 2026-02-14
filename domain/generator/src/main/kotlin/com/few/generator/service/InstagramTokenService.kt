package com.few.generator.service

import com.few.generator.domain.InstagramToken
import com.few.generator.repository.InstagramTokenRepository
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import java.time.LocalDateTime

@Service
class InstagramTokenService(
    private val instagramTokenRepository: InstagramTokenRepository,
) {
    private val log = KotlinLogging.logger {}

    @GeneratorTransactional(readOnly = true)
    fun getLatestAccessToken(): String {
        val token =
            instagramTokenRepository.findTopByOrderByCreatedAtDesc()
                ?: throw IllegalStateException("DB에 저장된 Instagram 토큰이 없습니다.")
        log.debug { "DB에서 Instagram 토큰 조회 성공 (만료시각: ${token.expiresAt})" }
        return token.accessToken
    }

    @GeneratorTransactional(propagation = Propagation.REQUIRES_NEW)
    fun saveNewToken(
        accessToken: String,
        expiresIn: Long,
    ): InstagramToken {
        val expiresAt = LocalDateTime.now().plusSeconds(expiresIn)
        val token =
            InstagramToken(
                accessToken = accessToken,
                expiresIn = expiresIn,
                expiresAt = expiresAt,
            )
        val saved = instagramTokenRepository.save(token)
        log.info { "Instagram 토큰 갱신 완료 (만료시각: $expiresAt)" }
        return saved
    }
}
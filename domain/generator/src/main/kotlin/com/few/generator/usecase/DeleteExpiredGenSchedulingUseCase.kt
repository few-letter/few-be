package com.few.generator.usecase

import com.few.generator.repository.GenRepository
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Component
class DeleteExpiredGenSchedulingUseCase(
    private val genRepository: GenRepository,
    @Value("\${generator.gen-cleanup.expiration-days}")
    private val expirationDays: Long,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    @GeneratorTransactional
    fun execute() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "만료된 Gen 삭제 작업이 이미 실행 중입니다." }
            return
        }

        try {
            val cutoff = LocalDateTime.now().minusDays(expirationDays)
            log.info { "생성된지 ${expirationDays}일이 지난 Gen 삭제 시작: cutoff=$cutoff" }

            val deletedCount = genRepository.deleteAllByCreatedAtBefore(cutoff)

            log.info { "만료된 Gen 삭제 완료: ${deletedCount}건 삭제" }
        } catch (e: Exception) {
            log.error(e) { "만료된 Gen 삭제 중 오류 발생" }
        } finally {
            isRunning.set(false)
        }
    }
}
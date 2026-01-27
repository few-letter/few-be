package com.few.generator.usecase

import com.few.generator.domain.vo.SendResult
import com.few.generator.service.GenService
import com.few.generator.service.specifics.newsletter.NewsletterContentAggregator
import com.few.generator.service.specifics.newsletter.NewsletterDelivery
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

@Component
class SendNewsletterSchedulingUseCase(
    private val genService: GenService,
    private val newsletterContentAggregator: NewsletterContentAggregator,
    private val newsletterDeliveryService: NewsletterDelivery,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    @GeneratorTransactional
    fun send() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "뉴스레터 스케줄링이 이미 실행 중입니다." }
            return
        }

        try {
            executeWithLogging()
        } finally {
            isRunning.set(false)
        }
    }

    fun execute(): SendResult {
        val latestGenDate =
            genService.findLatestGen().createdAt?.toLocalDate()
                ?: return SendResult(0, 0)

        val newsletterData = newsletterContentAggregator.prepareNewsletterData(latestGenDate)
        return newsletterDeliveryService.sendToSubscribers(newsletterData)
    }

    private fun executeWithLogging() {
        val startTime = LocalDateTime.now()
        var isSuccess = true
        var executionTimeSec = 0.0
        var exception: Throwable? = null
        var result = SendResult(0, 0)

        runCatching {
            executionTimeSec =
                measureTimeMillis {
                    result = execute()
                }.msToSeconds()
        }.onFailure { ex ->
            isSuccess = false
            log.error(ex) { "뉴스레터 전송 중 오류 발생" }
            exception = ex
        }.also {
            log.info {
                buildString {
                    appendLine("📧 뉴스레터 전송 완료")
                    appendLine("✅ 성공 여부: $isSuccess")
                    appendLine("✅ 시작 시간: $startTime")
                    appendLine("✅ 소요 시간: ${executionTimeSec}초")
                    appendLine("✅ 결과: 성공(${result.successCount}) / 실패(${result.failCount})")
                    if (!isSuccess) appendLine("❌ 오류: ${exception?.message}")
                }
            }
        }
    }

    private fun Long.msToSeconds(): Double = this / 1000.0
}
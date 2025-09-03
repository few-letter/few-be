package com.few.generator.usecase

import com.few.generator.service.MailSendService
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

@Component
class NewsletterSchedulingUseCase(
    private val mailSendService: MailSendService,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    @Scheduled(cron = "0 0 8 * * *")
    @GeneratorTransactional
    fun execute() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "뉴스레터 스케줄링이 이미 실행 중입니다." }
            return
        }

        try {
            doExecute()
        } finally {
            isRunning.set(false)
        }
    }

    private fun doExecute() {
        val startTime = LocalDateTime.now()
        var isSuccess = true
        var executionTimeSec = 0.0
        var exception: Throwable? = null

        var result: Pair<Int, Int> = Pair(0, 0)

        runCatching {
            executionTimeSec =
                measureTimeMillis {
                    result = mailSendService.sendDailyNewsletter()
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
                    appendLine("✅ 결과: 성공(${result.first}) / 실패(${result.second})")
                    if (!isSuccess) appendLine("❌ 오류: ${exception?.message}")
                }
            }
        }
    }

    private fun Long.msToSeconds(): Double = this / 1000.0
}
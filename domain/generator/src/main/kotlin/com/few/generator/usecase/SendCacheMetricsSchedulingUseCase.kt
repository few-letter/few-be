package com.few.generator.usecase

import com.few.generator.config.properties.CacheNames
import com.few.generator.event.GenCacheMetricsCollectFailedEvent
import com.few.generator.service.CacheMetricsCollector
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Component
class SendCacheMetricsSchedulingUseCase(
    private val cacheMetricsCollector: CacheMetricsCollector,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    /**
     * 매일 새벽 3시에 Gen 캐시 통계를 Slack으로 전송합니다.
     *
     * 참고: Micrometer의 cache metrics는 애플리케이션 시작 이후 누적 통계입니다.
     * 일별 통계를 원한다면 별도의 저장소에 이전 값을 저장하고 차이를 계산해야 합니다.
     */
    fun sendCacheMetrics() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "캐시 메트릭 전송이 이미 실행 중입니다." }
            return
        }

        try {
            execute()
        } catch (e: Exception) {
            log.error(e) { "캐시 메트릭 전송 중 오류 발생" }
        } finally {
            isRunning.set(false)
        }
    }

    fun execute() {
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        log.info { "캐시 메트릭 수집 시작: $now" }

        val genCacheStats = cacheMetricsCollector.collectGenCacheStats()

        if (genCacheStats == null) {
            log.warn { "${CacheNames.GEN_CACHE} 통계를 수집할 수 없습니다." }
            applicationEventPublisher.publishEvent(
                GenCacheMetricsCollectFailedEvent(
                    date = today,
                    errorMessage = "${CacheNames.GEN_CACHE} 통계를 수집할 수 없습니다.",
                ),
            )
            return
        }

        log.info {
            buildString {
                appendLine("📊 ${CacheNames.GEN_CACHE} 통계")
                appendLine("  Hits: ${genCacheStats.hits.toLong()}")
                appendLine("  Misses: ${genCacheStats.misses.toLong()}")
                appendLine("  Hit Rate: ${String.format("%.2f%%", genCacheStats.hitRate * 100)}")
                appendLine("  Puts: ${genCacheStats.puts.toLong()}")
                appendLine("  Evictions: ${genCacheStats.evictions.toLong()}")
            }
        }
    }
}
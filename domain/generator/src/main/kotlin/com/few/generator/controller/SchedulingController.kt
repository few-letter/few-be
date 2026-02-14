package com.few.generator.controller

import com.few.generator.usecase.GlobalGenSchedulingUseCase
import com.few.generator.usecase.LocalGenSchedulingUseCase
import com.few.generator.usecase.RefreshInstagramTokenUseCase
import com.few.generator.usecase.SendCacheMetricsSchedulingUseCase
import com.few.generator.usecase.SendNewsletterSchedulingUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SchedulingController(
    private val localGenSchedulingUseCase: LocalGenSchedulingUseCase,
    private val globalGenSchedulingUseCase: GlobalGenSchedulingUseCase,
    private val sendCacheMetricsSchedulingUseCase: SendCacheMetricsSchedulingUseCase,
    private val sendNewsletterSchedulingUseCase: SendNewsletterSchedulingUseCase,
    private val refreshInstagramTokenUseCase: RefreshInstagramTokenUseCase,
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "\${scheduling.cron.local-gen}")
    fun createLocalNewsContents() {
        localGenSchedulingUseCase.executeAsync()
    }

    @Scheduled(cron = "\${scheduling.cron.global-gen}")
    fun createGlobalNewsContents() {
        globalGenSchedulingUseCase.executeAsync()
    }

    @Scheduled(cron = "\${scheduling.cron.cache-metrics}", zone = "Asia/Seoul")
    fun sendCacheMetrics() {
        sendCacheMetricsSchedulingUseCase.sendCacheMetrics()
    }

    @Scheduled(cron = "\${scheduling.cron.email}", zone = "Asia/Seoul")
    fun sendEmail() {
        sendNewsletterSchedulingUseCase.send()
    }

    @Scheduled(cron = "\${scheduling.cron.instagram-token-refresh}", zone = "Asia/Seoul")
    fun refreshInstagramToken() {
        try {
            refreshInstagramTokenUseCase.execute()
        } catch (e: Exception) {
            log.error(e) { "Instagram 토큰 갱신 스케줄 실행 중 오류 발생: ${e.message}" }
        }
    }
}
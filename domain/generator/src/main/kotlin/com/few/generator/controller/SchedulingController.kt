package com.few.generator.controller

import com.few.generator.usecase.GlobalGenSchedulingUseCase
import com.few.generator.usecase.LocalGenSchedulingUseCase
import com.few.generator.usecase.SendCacheMetricsSchedulingUseCase
import com.few.generator.usecase.SendNewsletterSchedulingUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SchedulingController(
    private val localGenSchedulingUseCase: LocalGenSchedulingUseCase,
    private val globalGenSchedulingUseCase: GlobalGenSchedulingUseCase,
    private val sendCacheMetricsSchedulingUseCase: SendCacheMetricsSchedulingUseCase,
    private val sendNewsletterSchedulingUseCase: SendNewsletterSchedulingUseCase,
) {
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
}
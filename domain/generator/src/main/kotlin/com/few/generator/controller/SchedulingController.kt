package com.few.generator.controller

import com.few.generator.usecase.GlobalGenSchedulingUseCase
import com.few.generator.usecase.LocalGenSchedulingUseCase
import com.few.generator.usecase.PopularNasdaqStockScrapingSchedulingUseCase
import com.few.generator.usecase.RefreshInstagramTokenUseCase
import com.few.generator.usecase.SendCacheMetricsSchedulingUseCase
import com.few.generator.usecase.SendNewsletterSchedulingUseCase
import com.few.generator.usecase.StockBriefingSchedulingUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SchedulingController(
    private val localGenSchedulingUseCase: LocalGenSchedulingUseCase,
    private val globalGenSchedulingUseCase: GlobalGenSchedulingUseCase,
    private val sendCacheMetricsSchedulingUseCase: SendCacheMetricsSchedulingUseCase,
    private val sendNewsletterSchedulingUseCase: SendNewsletterSchedulingUseCase,
    private val refreshInstagramTokenUseCase: RefreshInstagramTokenUseCase,
    private val stockBriefingSchedulingUseCase: StockBriefingSchedulingUseCase,
    private val popularNasdaqStockScrapingSchedulingUseCase: PopularNasdaqStockScrapingSchedulingUseCase,
) {
    // ===== Contents Publishing Scheduling Area Start =====
    @Scheduled(cron = "\${scheduling.cron.local-gen}", zone = "Asia/Seoul")
    fun createLocalNewsContents() {
        localGenSchedulingUseCase.executeAsync(true)
    }

    @Scheduled(cron = "\${scheduling.cron.global-gen}", zone = "Asia/Seoul")
    fun createGlobalNewsContents() {
        globalGenSchedulingUseCase.executeAsync(true)
    }

    @Scheduled(cron = "\${scheduling.cron.stock-briefing}", zone = "Asia/Seoul")
    fun crawlStockBriefing() {
        stockBriefingSchedulingUseCase.executeAsync()
    }

    @Scheduled(cron = "\${scheduling.cron.popular-nasdaq-stock-news}", zone = "Asia/Seoul")
    fun scrapeTimeEtf() {
        popularNasdaqStockScrapingSchedulingUseCase.executeAsync()
    }
    // ===== Contents Publishing Scheduling Area End =====

    // ===== Extra Scheduling Area Start =====
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
        refreshInstagramTokenUseCase.execute()
    }
    // ===== Extra Scheduling Area End =====
}
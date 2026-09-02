package com.few.generator.controller

import com.few.generator.config.properties.SchedulingProperties
import com.few.generator.usecase.GlobalGenSchedulingUseCase
import com.few.generator.usecase.LocalGenSchedulingUseCase
import com.few.generator.usecase.PopularNasdaqStockScrapingSchedulingUseCase
import com.few.generator.usecase.RefreshInstagramTokenUseCase
import com.few.generator.usecase.SendCacheMetricsSchedulingUseCase
import com.few.generator.usecase.SendNewsletterSchedulingUseCase
import com.few.generator.usecase.StockBriefingSchedulingUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SchedulingController(
    private val schedulingProperties: SchedulingProperties,
    private val localGenSchedulingUseCase: LocalGenSchedulingUseCase,
    private val globalGenSchedulingUseCase: GlobalGenSchedulingUseCase,
    private val sendCacheMetricsSchedulingUseCase: SendCacheMetricsSchedulingUseCase,
    private val sendNewsletterSchedulingUseCase: SendNewsletterSchedulingUseCase,
    private val refreshInstagramTokenUseCase: RefreshInstagramTokenUseCase,
    private val stockBriefingSchedulingUseCase: StockBriefingSchedulingUseCase,
    private val popularNasdaqStockScrapingSchedulingUseCase: PopularNasdaqStockScrapingSchedulingUseCase,
) {
    private val log = KotlinLogging.logger {}

    // ===== Contents Publishing Scheduling Area Start =====
    @Scheduled(cron = "\${scheduling.local-gen.cron:-}", zone = "Asia/Seoul")
    fun createLocalNewsContents() {
        if (isDisabled("local-gen", schedulingProperties.localGen.enabled)) return
        localGenSchedulingUseCase.executeAsync(true)
    }

    @Scheduled(cron = "\${scheduling.global-gen.cron:-}", zone = "Asia/Seoul")
    fun createGlobalNewsContents() {
        if (isDisabled("global-gen", schedulingProperties.globalGen.enabled)) return
        globalGenSchedulingUseCase.executeAsync(true)
    }

    @Scheduled(cron = "\${scheduling.stock-briefing.cron:-}", zone = "Asia/Seoul")
    fun crawlStockBriefing() {
        if (isDisabled("stock-briefing", schedulingProperties.stockBriefing.enabled)) return
        stockBriefingSchedulingUseCase.executeAsync()
    }

    @Scheduled(cron = "\${scheduling.popular-nasdaq-stock-news.cron:-}", zone = "Asia/Seoul")
    fun scrapeTimeEtf() {
        if (isDisabled("popular-nasdaq-stock-news", schedulingProperties.popularNasdaqStockNews.enabled)) return
        popularNasdaqStockScrapingSchedulingUseCase.executeAsync()
    }
    // ===== Contents Publishing Scheduling Area End =====

    // ===== Extra Scheduling Area Start =====
    @Scheduled(cron = "\${scheduling.cache-metrics.cron:-}", zone = "Asia/Seoul")
    fun sendCacheMetrics() {
        if (isDisabled("cache-metrics", schedulingProperties.cacheMetrics.enabled)) return
        sendCacheMetricsSchedulingUseCase.sendCacheMetrics()
    }

    @Scheduled(cron = "\${scheduling.email.cron:-}", zone = "Asia/Seoul")
    fun sendEmail() {
        if (isDisabled("email", schedulingProperties.email.enabled)) return
        sendNewsletterSchedulingUseCase.send()
    }

    @Scheduled(cron = "\${scheduling.instagram-token-refresh.cron:-}", zone = "Asia/Seoul")
    fun refreshInstagramToken() {
        if (isDisabled("instagram-token-refresh", schedulingProperties.instagramTokenRefresh.enabled)) return
        refreshInstagramTokenUseCase.execute()
    }
    // ===== Extra Scheduling Area End =====

    private fun isDisabled(
        name: String,
        enabled: Boolean,
    ): Boolean {
        if (!enabled) {
            log.info { "Scheduling '$name' is disabled. Skip execution." }
            return true
        }
        return false
    }
}
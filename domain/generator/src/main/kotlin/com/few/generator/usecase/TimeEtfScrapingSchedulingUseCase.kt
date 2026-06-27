package com.few.generator.usecase

import com.few.generator.core.scrapper.timefolio.TimeEtfScrapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
class TimeEtfScrapingSchedulingUseCase(
    private val timeEtfScrapper: TimeEtfScrapper,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    @Async("generatorSchedulingExecutor")
    fun executeAsync() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "TimeETF 스크래핑 스케줄링이 이미 실행 중입니다." }
            return
        }

        try {
            execute()
        } catch (e: Exception) {
            log.error(e) { "TimeETF 스크래핑 스케줄링 실행 중 오류: ${e.message}" }
        } finally {
            isRunning.set(false)
        }
    }

    fun execute() {
        val items = timeEtfScrapper.scrapeTopItems()
        if (items.isEmpty()) {
            log.warn { "TimeETF 스크래핑 결과가 없습니다." }
            return
        }

        items.forEach { item ->
            log.info { "TimeETF 종목 [${item.rank}위] ticker=${item.ticker}, 종목명=${item.stockName}" }
        }
    }
}
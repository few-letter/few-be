package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.alphavantage.AlphaVantageClient
import com.few.generator.core.alphavantage.AlphaVantageNewsFeed
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.core.scrapper.timefolio.TimeEtfScrapper
import com.few.generator.domain.Gen
import com.few.generator.service.GenService
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
class TimeEtfScrapingSchedulingUseCase(
    private val timeEtfScrapper: TimeEtfScrapper,
    private val alphaVantageClient: AlphaVantageClient,
    private val chatGpt: ChatGpt,
    private val promptGenerator: PromptGenerator,
    private val genService: GenService,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
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
        val popularNasdaqStocks = timeEtfScrapper.scrapeTopItems()
        if (popularNasdaqStocks.isEmpty()) {
            log.warn { "TimeETF 스크래핑 결과가 없습니다." }
            return
        }

        log.info { "TimeETF 구성 종목 ${popularNasdaqStocks.size}개에 대한 뉴스 조회 시작" }

        popularNasdaqStocks.forEach { stock ->
            log.info { "TimeETF [${stock.rank}위] ${stock.ticker} (${stock.stockName}) 뉴스 조회 중" }

            val feeds =
                try {
                    alphaVantageClient.getTopNewsFeed(stock.ticker)
                } catch (e: Exception) {
                    log.error(e) { "AlphaVantage 뉴스 조회 실패: ticker=${stock.ticker}" }
                    return@forEach
                }

            if (feeds.isEmpty()) {
                log.warn { "AlphaVantage 뉴스 결과 없음: ticker=${stock.ticker}" }
                return@forEach
            }

            feeds.forEach { feed ->
                processAndSaveFeed(stock.ticker, feed)
            }
        }

        log.info { "TimeETF 스케줄링 완료" }
    }

    private fun processAndSaveFeed(
        ticker: String,
        feed: AlphaVantageNewsFeed,
    ) {
        if (genService.findByUrl(feed.url) != null) {
            log.info { "이미 저장된 뉴스 URL, 건너뜀: ${feed.url}" }
            return
        }

        val headline =
            try {
                (chatGpt.ask(promptGenerator.toKoreanShortHeadline(feed.title)) as? Headline)?.headline
                    ?: feed.title
            } catch (e: Exception) {
                log.error(e) { "헤드라인 번역 GPT 호출 실패: ${feed.title}" }
                return
            }

        val summary =
            try {
                (chatGpt.ask(promptGenerator.toKoreanShortSummary(feed.summary)) as? Summary)?.summary
                    ?: feed.summary
            } catch (e: Exception) {
                log.error(e) { "요약 번역 GPT 호출 실패: ticker=$ticker, title=${feed.title}" }
                return
            }

        genService.saveWithNewTx(
            Gen(
                url = feed.url,
                thumbnailImageUrl = feed.bannerImage,
                mediaType = MediaType.ETC.code,
                headline = headline,
                summary = summary,
                highlightTexts = "[]",
                coreTextsJson = gson.toJson(listOf(feed.summary)),
                category = Category.ECONOMY.code,
                region = Region.GLOBAL.code,
            ),
        )

        log.info { "Gen 저장 완료: ticker=$ticker, headline=$headline" }
    }
}
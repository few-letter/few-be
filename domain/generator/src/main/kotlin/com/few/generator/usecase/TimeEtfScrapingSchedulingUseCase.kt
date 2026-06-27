package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.alphavantage.AlphaVantageClient
import com.few.generator.core.alphavantage.AlphaVantageNewsFeedItem
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
        val items = timeEtfScrapper.scrapeTopItems()
        if (items.isEmpty()) {
            log.warn { "TimeETF 스크래핑 결과가 없습니다." }
            return
        }

        log.info { "TimeETF 구성 종목 ${items.size}개에 대한 뉴스 조회 시작" }

        items.forEach { etfItem ->
            log.info { "TimeETF [${etfItem.rank}위] ${etfItem.ticker} (${etfItem.stockName}) 뉴스 조회 중" }

            val feedItems =
                try {
                    alphaVantageClient.getTopNewsFeed(etfItem.ticker)
                } catch (e: Exception) {
                    log.error(e) { "AlphaVantage 뉴스 조회 실패: ticker=${etfItem.ticker}" }
                    return@forEach
                }

            if (feedItems.isEmpty()) {
                log.warn { "AlphaVantage 뉴스 결과 없음: ticker=${etfItem.ticker}" }
                return@forEach
            }

            feedItems.forEach { feedItem ->
                processAndSaveFeedItem(etfItem.ticker, feedItem)
            }
        }

        log.info { "TimeETF 스케줄링 완료" }
    }

    private fun processAndSaveFeedItem(
        ticker: String,
        feedItem: AlphaVantageNewsFeedItem,
    ) {
        if (genService.findByUrl(feedItem.url) != null) {
            log.info { "이미 저장된 뉴스 URL, 건너뜀: ${feedItem.url}" }
            return
        }

        val headline =
            try {
                (chatGpt.ask(promptGenerator.toKoreanShortHeadline(feedItem.title)) as? Headline)?.headline
                    ?: feedItem.title
            } catch (e: Exception) {
                log.error(e) { "헤드라인 번역 GPT 호출 실패: ${feedItem.title}" }
                return
            }

        val summary =
            try {
                (chatGpt.ask(promptGenerator.toKoreanShortSummary(feedItem.summary)) as? Summary)?.summary
                    ?: feedItem.summary
            } catch (e: Exception) {
                log.error(e) { "요약 번역 GPT 호출 실패: ticker=$ticker, title=${feedItem.title}" }
                return
            }

        genService.save(
            Gen(
                url = feedItem.url,
                thumbnailImageUrl = feedItem.bannerImage,
                mediaType = MediaType.ETC.code,
                headline = headline,
                summary = summary,
                highlightTexts = "[]",
                coreTextsJson = gson.toJson(listOf(feedItem.summary)),
                category = Category.ECONOMY.code,
                region = Region.GLOBAL.code,
            ),
        )

        log.info { "Gen 저장 완료: ticker=$ticker, headline=$headline" }
    }
}
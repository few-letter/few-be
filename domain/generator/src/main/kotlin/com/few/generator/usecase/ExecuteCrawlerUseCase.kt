package com.few.generator.usecase

import com.few.generator.core.*
import com.few.generator.core.crawler.NaverNewsCrawler
import com.few.generator.core.model.ContentSpec
import com.few.generator.usecase.dto.ExecuteCrawlerUseCaseIn
import com.few.generator.usecase.dto.ExecuteCrawlerUseCaseOut
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.*

@Component
class ExecuteCrawlerUseCase(
    private val crawlers: List<Crawler>,
    private val extractor: Extractor,
    private val grouper: Grouper,
    private val summarizer: Summarizer,
) {
    private val log = KotlinLogging.logger {}

    // TODO: @Transactional
    fun execute(useCaseIn: ExecuteCrawlerUseCaseIn): ExecuteCrawlerUseCaseOut =
        runBlocking {
            val crawler = getCrawler(useCaseIn)
            val urls =
                getUrls(useCaseIn, crawler).apply {
                    log.info { "Start crawling ${this.size} news." }
                }

            val contents = mutableListOf<ContentSpec>()
            for ((i, url) in urls.withIndex()) {
                val content = crawler.execute(url)
                if (content != null) {
                    contents.add(content)
                }
                log.info { "Completed processing ${i + 1}/${urls.size}" }
            }
            log.info { "Crawling completed. ${contents.size} crawled." }

            /**
             * 2. 뉴스 추출 및 요약
             */
            val extractedContent =
                extractor.execute(contents).apply {
                    log.info { "Extracted ${this.size} news." }
                }

            /**
             * 3. 뉴스 그룹화
             */
            val groupedContent =
                grouper.execute(extractedContent).apply {
                    log.info { "Grouped ${this.size} news." }
                }

            /**
             * 4. 그룹 뉴스 요약
             */
            val summarizedGroups =
                summarizer.execute(groupedContent).apply {
                    log.info { "Summarized ${this.size} news." }
                }

            ExecuteCrawlerUseCaseOut(
                useCaseIn.sid,
                listOf(UUID.randomUUID().toString()), // TODO: DB 저장 시 크롤링 고유 ID 응답
            )
        }

    private fun getCrawler(useCaseIn: ExecuteCrawlerUseCaseIn): Crawler = crawlers.first { it is NaverNewsCrawler } as NaverNewsCrawler

    private fun getUrls(
        useCaseIn: ExecuteCrawlerUseCaseIn,
        crawler: Crawler,
    ): List<String> {
        if (crawler is NaverNewsCrawler) {
            log.info { "Get urls from Naver News for sid: ${useCaseIn.sid}" }
            return crawler.getUrls(useCaseIn.sid).apply {
                log.info { "urls:\n ${this.joinToString("\n")}" }
            }
        }
        return emptyList()
    }
}
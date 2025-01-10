package com.few.generator.usecase

import com.few.generator.core.Crawler
import com.few.generator.core.crawler.NaverNewsCrawler
import com.few.generator.core.model.ContentSpec
import com.few.generator.domain.ContentSource
import com.few.generator.domain.CrawlUrl
import com.few.generator.repository.ContentSourceRepository
import com.few.generator.repository.CrawlUrlRepository
import com.few.generator.service.ConvertContentWithAIService
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.dto.ExecuteCrawlerUseCaseIn
import com.few.generator.usecase.dto.ExecuteCrawlerUseCaseOut
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ExecuteCrawlerUseCase(
    private val crawlUrlRepository: CrawlUrlRepository,
    private val contentSourceRepository: ContentSourceRepository,
    private val crawlers: List<Crawler>,
    private val convertContentWithAIService: ConvertContentWithAIService,
) {
    private val log = KotlinLogging.logger {}

    @GeneratorTransactional
    fun execute(useCaseIn: ExecuteCrawlerUseCaseIn): ExecuteCrawlerUseCaseOut {
        val crawler = getCrawler(useCaseIn)
        val urls =
            getUrls(useCaseIn, crawler).apply {
                log.info { "Start crawling ${this.size} news." }
            }

        val crawlUrl =
            crawlUrlRepository.save(
                CrawlUrl(
                    urls = urls,
                ),
            )

        val contents = mutableListOf<ContentSpec>()
        for ((i, url) in urls.withIndex()) {
            val content = crawler.execute(url)
            if (content != null) {
                contents.add(content)
            }
            log.info { "Completed processing ${i + 1}/${urls.size}" }
        }
        log.info { "Crawling completed. ${contents.size} crawled." }

        val aiContent = convertContentWithAIService.execute(contents)
        val contentSource =
            contentSourceRepository.save(
                ContentSource(
                    source = aiContent,
                    crawlUrlId = crawlUrl.id!!,
                ),
            )

        return ExecuteCrawlerUseCaseOut(
            crawlUrlId = crawlUrl.id!!,
            contentSourceId = contentSource.id!!,
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
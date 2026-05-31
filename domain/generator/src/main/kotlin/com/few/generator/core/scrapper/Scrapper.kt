package com.few.generator.core.scrapper

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.core.scrapper.cnbc.CnbcNewsScrapper
import com.few.generator.core.scrapper.naver.NaverNewsScrapper
import com.few.generator.core.scrapper.naver.NaverStockBriefingScrapper
import com.few.generator.core.scrapper.naver.StockBriefingRawContent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class Scrapper(
    private val naverNewsScrapper: NaverNewsScrapper,
    private val cnbcNewsScrapper: CnbcNewsScrapper,
    private val naverStockBriefingScrapper: NaverStockBriefingScrapper,
) {
    private val log = KotlinLogging.logger {}

    fun extractUrlsByCategories(region: Region): Map<Category, List<String>> =
        when (region) {
            Region.LOCAL ->
                naverNewsScrapper
                    .getRootUrlsByCategory(Category.entries)
                    .mapValues { (_, rootUrl) ->
                        naverNewsScrapper.extractUrlsByCategory(rootUrl)
                    }
            Region.GLOBAL ->
                cnbcNewsScrapper
                    .getRootUrlsByCategory(Category.entries)
                    .mapValues { (_, rootUrl) ->
                        cnbcNewsScrapper.extractUrlsByCategory(rootUrl)
                    }
        }

    fun scrape(url: String): ScrappedResult {
        Thread.sleep((1..5).random().toLong())

        if (url.contains("naver.com")) {
            return naverNewsScrapper.scrape(url)
        } else if (url.contains("cnbc.com")) {
            return cnbcNewsScrapper.scrape(url)
        } else {
            throw RuntimeException("Only support naver.com and cnbc.com yet")
        }
    }

    fun checkStockBriefingPostExists(postId: Long): Boolean = naverStockBriefingScrapper.checkPostExists(postId)

    fun scrapeStockBriefingPost(postId: Long): List<StockBriefingRawContent> = naverStockBriefingScrapper.scrapePost(postId)
}
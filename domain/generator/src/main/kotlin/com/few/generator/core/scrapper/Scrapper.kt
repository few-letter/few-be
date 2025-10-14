package com.few.generator.core.scrapper

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.core.scrapper.cnbc.CnbcScrapper
import com.few.generator.core.scrapper.naver.NaverScrapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class Scrapper(
    private val naverScrapper: NaverScrapper,
    private val cnbcScrapper: CnbcScrapper,
) {
    private val log = KotlinLogging.logger {}

    fun extractUrlsByCategories(region: Region): Map<Category, List<String>> =
        when (region) {
            Region.LOCAL ->
                naverScrapper
                    .getRootUrlsByCategory(Category.entries)
                    .mapValues { (_, rootUrl) ->
                        naverScrapper.extractUrlsByCategory(rootUrl)
                    }
            Region.GLOBAL ->
                cnbcScrapper
                    .getRootUrlsByCategory(Category.entries)
                    .mapValues { (_, rootUrl) ->
                        cnbcScrapper.extractUrlsByCategory(rootUrl)
                    }
        }

    fun scrape(url: String): ScrappedResult {
        Thread.sleep((1..5).random().toLong())

        if (url.contains("naver.com")) {
            return naverScrapper.scrape(url)
        } else if (url.contains("cnbc.com")) {
            return cnbcScrapper.scrape(url)
        } else {
            throw RuntimeException("Only support naver.com and cnbc.com yet")
        }
    }
}
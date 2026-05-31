package com.few.generator.service.specifics.newsletter

import com.few.common.domain.MediaType
import com.few.generator.domain.Gen
import com.few.generator.domain.vo.NewsletterData
import com.few.generator.service.GenService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class NewsletterContentAggregator(
    private val genService: GenService,
) {
    fun prepareNewsletterData(targetDate: LocalDate): NewsletterData {
        val dailyGens = collectDailyGens(targetDate)
        val gensByCategory = dailyGens.groupBy { it.category }
        val rawContentsUrlsByGens = dailyGens.mapNotNull { gen -> gen.id?.let { it to gen.url } }.toMap()
        val rawContentsMediaTypeByGens =
            dailyGens
                .mapNotNull { gen -> gen.id?.let { it to MediaType.from(gen.mediaType).title } }
                .toMap()

        return NewsletterData(
            targetDate = targetDate,
            gensByCategory = gensByCategory,
            rawContentsUrlsByGens = rawContentsUrlsByGens,
            rawContentsMediaTypeNameByGens = rawContentsMediaTypeByGens,
        )
    }

    private fun collectDailyGens(targetDate: LocalDate): List<Gen> =
        genService.findAllByCreatedAtBetweenAndRegion(
            start = targetDate.atStartOfDay(),
            end = targetDate.plusDays(1).atStartOfDay(),
        )
}
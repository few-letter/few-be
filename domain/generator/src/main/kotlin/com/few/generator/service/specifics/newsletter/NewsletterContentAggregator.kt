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
        val gensByCategory = groupGensByCategory(dailyGens)
        val urlsByGenId = extractUrlsByGenId(dailyGens)
        val mediaTypeNamesByGenId = extractMediaTypeNamesByGenId(dailyGens)

        return NewsletterData(
            targetDate = targetDate,
            gensByCategory = gensByCategory,
            rawContentsUrlsByGens = urlsByGenId,
            rawContentsMediaTypeNameByGens = mediaTypeNamesByGenId,
        )
    }

    private fun collectDailyGens(targetDate: LocalDate): List<Gen> =
        genService.findAllByCreatedAtBetweenAndRegion(
            start = targetDate.atStartOfDay(),
            end = targetDate.plusDays(1).atStartOfDay(),
        )

    private fun groupGensByCategory(gens: List<Gen>): Map<Int, List<Gen>> = gens.groupBy { it.category }

    private fun extractUrlsByGenId(gens: List<Gen>): Map<Long, String> = gens.mapNotNull { gen -> gen.id?.let { it to gen.url } }.toMap()

    private fun extractMediaTypeNamesByGenId(gens: List<Gen>): Map<Long, String> =
        gens
            .mapNotNull { gen ->
                gen.id?.let { it to MediaType.from(gen.mediaType).title }
            }.toMap()
}
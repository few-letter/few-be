package com.few.generator.service.specifics.newsletter

import com.few.common.domain.MediaType
import com.few.generator.domain.Gen
import com.few.generator.domain.RawContents
import com.few.generator.domain.vo.NewsletterData
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class NewsletterContentAggregator(
    private val genService: GenService,
    private val provisioningService: ProvisioningService,
    private val rawContentsService: RawContentsService,
) {
    fun prepareNewsletterData(targetDate: LocalDate): NewsletterData {
        val dailyGens = collectDailyGens(targetDate)
        val gensByCategory = groupGensByCategory(dailyGens)
        val rawContentsByGenId = enrichWithRawContents(dailyGens)
        val rawContentsUrlsByGens = extractUrlsFromRawContents(rawContentsByGenId)
        val rawContentsMediaTypeByGens = extractMediaTypeNamesFromRawContents(rawContentsByGenId)

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

    private fun groupGensByCategory(gens: List<Gen>): Map<Int, List<Gen>> = gens.groupBy { it.category }

    private fun enrichWithRawContents(gens: List<Gen>): Map<Long, RawContents> {
        if (gens.isEmpty()) return emptyMap()

        val provisioningIds = gens.map { it.provisioningContentsId }.distinct()
        if (provisioningIds.isEmpty()) return emptyMap()

        val provisioningContents = provisioningService.findAllByIdIn(provisioningIds)
        val provisioningById =
            provisioningContents
                .mapNotNull { provisioning -> provisioning.id?.let { provisioning.id to provisioning } }
                .toMap()

        val rawContentsIds = provisioningContents.map { it.rawContentsId }.distinct()
        if (rawContentsIds.isEmpty()) return emptyMap()

        val rawContents = rawContentsService.findAllByIdIn(rawContentsIds)
        val rawContentsById =
            rawContents
                .mapNotNull { rawContent -> rawContent.id?.let { it to rawContent } }
                .toMap()

        return gens
            .mapNotNull { gen ->
                val pId = gen.provisioningContentsId
                val rawId = provisioningById[pId]?.rawContentsId ?: return@mapNotNull null
                val rawContents = rawContentsById[rawId]
                rawContents?.let { gen.id?.let { genId -> genId to rawContents } }
            }.toMap()
    }

    private fun extractUrlsFromRawContents(rawContentsById: Map<Long, RawContents>): Map<Long, String> =
        rawContentsById.mapValues { it.value.url }

    private fun extractMediaTypeNamesFromRawContents(rawContentsById: Map<Long, RawContents>): Map<Long, String> =
        rawContentsById.mapValues {
            val mediaType = MediaType.from(it.value.mediaType)
            mediaType.title
        }
}
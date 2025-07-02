package com.few.generator.service

import com.few.generator.controller.response.BrowseGroupGenResponse
import com.few.generator.controller.response.BrowseGroupGenResponses
import com.few.generator.controller.response.GroupSourceHeadlineData
import com.few.generator.domain.GroupGen
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class GroupGenResponseMappingService(
    private val jsonParsingService: JsonParsingService,
) {
    private val log = KotlinLogging.logger {}

    fun mapToResponses(groupGens: List<GroupGen>): BrowseGroupGenResponses {
        log.debug { "GroupGen 응답 매핑 시작: ${groupGens.size}개" }

        val responses = groupGens.map { mapToResponse(it) }

        log.debug { "GroupGen 응답 매핑 완료" }
        return BrowseGroupGenResponses(groups = responses)
    }

    private fun mapToResponse(groupGen: GroupGen): BrowseGroupGenResponse =
        BrowseGroupGenResponse(
            id = groupGen.id!!,
            category = groupGen.category,
            groupIndices = groupGen.groupIndices,
            headline = groupGen.headline,
            summary = groupGen.summary,
            highlightTexts = jsonParsingService.parseHighlightTexts(groupGen.highlightTexts),
            groupSourceHeadlines = mapGroupSourceHeadlines(groupGen),
            createdAt = groupGen.createdAt!!,
        )

    private fun mapGroupSourceHeadlines(groupGen: GroupGen): List<GroupSourceHeadlineData> =
        groupGen.groupSourceHeadlines.map { sourceHeadline ->
            GroupSourceHeadlineData(
                headline = sourceHeadline.headline,
                url = sourceHeadline.url,
            )
        }
}
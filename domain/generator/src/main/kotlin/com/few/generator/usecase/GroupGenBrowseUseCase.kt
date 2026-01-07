package com.few.generator.usecase

import com.few.generator.config.GeneratorGsonConfig
import com.few.generator.controller.response.BrowseGroupGenResponse
import com.few.generator.controller.response.BrowseGroupGenResponses
import com.few.generator.controller.response.GroupSourceHeadlineData
import com.few.generator.repository.GroupGenRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.input.BrowseGroupGenUseCaseIn
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.LocalTime

@Component
data class GroupGenBrowseUseCase(
    private val groupGenRepository: GroupGenRepository,
    @Qualifier(GeneratorGsonConfig.GSON_BEAN_NAME)
    private val gson: Gson,
) {
    @GeneratorTransactional(readOnly = true)
    fun execute(useCaseIn: BrowseGroupGenUseCaseIn): BrowseGroupGenResponses {
        val mostRecentGroupGen =
            groupGenRepository.findFirstByRegionOrderByCreatedAtDesc(useCaseIn.region.code)
                ?: return BrowseGroupGenResponses(groups = emptyList())

        val mostRecentDate = mostRecentGroupGen.createdAt!!.toLocalDate()
        val start = mostRecentDate.atStartOfDay()
        val end = mostRecentDate.atTime(LocalTime.MAX)
        val groupGens =
            groupGenRepository
                .findAllByCreatedAtBetweenAndRegion(start, end, useCaseIn.region.code)

        return BrowseGroupGenResponses(
            groups =
                groupGens.map { groupGen ->
                    BrowseGroupGenResponse(
                        id = groupGen.id!!,
                        category = groupGen.category,
                        selectedGroupIds = groupGen.selectedGroupIds,
                        headline = groupGen.headline,
                        summary = groupGen.summary,
                        highlightTexts =
                            groupGen.highlightTexts.let { raw ->
                                if (raw.isBlank()) {
                                    emptyList()
                                } else {
                                    raw
                                        .removePrefix("[")
                                        .removeSuffix("]")
                                        .split(",")
                                        .map { it.trim().removeSurrounding("\"") }
                                        .filter { it.isNotBlank() }
                                }
                            },
                        groupSourceHeadlines =
                            try {
                                val type = object : TypeToken<List<GroupSourceHeadlineData>>() {}.type
                                gson.fromJson<List<GroupSourceHeadlineData>>(groupGen.groupSourceHeadlines, type) ?: emptyList()
                            } catch (_: Exception) {
                                emptyList()
                            },
                        createdAt = groupGen.createdAt!!,
                    )
                },
        )
    }
}
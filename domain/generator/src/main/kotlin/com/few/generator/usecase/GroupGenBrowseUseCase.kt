package com.few.generator.usecase

import com.few.generator.controller.response.BrowseGroupGenResponse
import com.few.generator.controller.response.BrowseGroupGenResponses
import com.few.generator.controller.response.GroupSourceHeadlineData
import com.few.generator.repository.GroupGenRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime

@Component
data class GroupGenBrowseUseCase(
    private val groupGenRepository: GroupGenRepository,
    private val gson: Gson,
) {
    fun execute(date: LocalDate?): BrowseGroupGenResponses {
        val targetDate = date ?: LocalDate.now()
        val start = targetDate.atStartOfDay()
        val end = targetDate.atTime(LocalTime.MAX)
        val groupGens =
            groupGenRepository
                .findAllByCreatedAtBetween(start, end)
                .sortedByDescending { it.createdAt }

        return BrowseGroupGenResponses(
            groups =
                groupGens.map { it ->
                    BrowseGroupGenResponse(
                        id = it.id!!,
                        category = it.category,
                        selectedGroupIds = it.selectedGroupIds,
                        headline = it.headline,
                        summary = it.summary,
                        highlightTexts =
                            it.highlightTexts.let { raw ->
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
                                gson.fromJson<List<GroupSourceHeadlineData>>(it.groupSourceHeadlines, type) ?: emptyList()
                            } catch (_: Exception) {
                                emptyList()
                            },
                        createdAt = it.createdAt!!,
                    )
                },
        )
    }
}
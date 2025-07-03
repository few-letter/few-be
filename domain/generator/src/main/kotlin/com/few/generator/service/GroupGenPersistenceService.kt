package com.few.generator.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.few.generator.domain.Category
import com.few.generator.domain.GroupGen
import com.few.generator.repository.GroupGenRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class GroupGenPersistenceService(
    private val groupGenRepository: GroupGenRepository,
    private val objectMapper: ObjectMapper,
) {
    private val log = KotlinLogging.logger {}

    fun saveGroupGen(
        category: Category,
        groupContent: GroupContentResult,
    ): GroupGen {
        log.debug { "GroupGen 저장 시작: category=${category.title}" }

        val groupGen = createGroupGenEntity(category, groupContent)
        val savedGroupGen = groupGenRepository.save(groupGen)

        log.info { "GroupGen 저장 완료: id=${savedGroupGen.id}, category=${category.title}" }
        return savedGroupGen
    }

    private fun createGroupGenEntity(
        category: Category,
        groupContentResult: GroupContentResult,
    ): GroupGen =
        GroupGen(
            category = category.code,
            groupIndices = formatGroupIndices(groupContentResult.group.group),
            headline = groupContentResult.groupHeadline.headline,
            summary = groupContentResult.groupSummary.summary,
            highlightTexts = formatHighlightTexts(groupContentResult.groupHighlights.highlightTexts),
            groupSourceHeadlines = groupContentResult.groupSourceHeadlines,
        )

    private fun formatGroupIndices(indices: List<Int>): String = objectMapper.writeValueAsString(indices)

    private fun formatHighlightTexts(highlightTexts: List<String>): String = objectMapper.writeValueAsString(highlightTexts)
}
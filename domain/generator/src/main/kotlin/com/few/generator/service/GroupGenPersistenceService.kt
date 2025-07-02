package com.few.generator.service

import com.few.generator.domain.Category
import com.few.generator.domain.GroupGen
import com.few.generator.repository.GroupGenRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class GroupGenPersistenceService(
    private val groupGenRepository: GroupGenRepository,
) {
    private val log = KotlinLogging.logger {}

    fun saveGroupGen(
        category: Category,
        groupPromptResult: GroupPromptResult,
    ): GroupGen {
        log.debug { "GroupGen 저장 시작: category=${category.title}" }

        val groupGen = createGroupGenEntity(category, groupPromptResult)
        val savedGroupGen = groupGenRepository.save(groupGen)

        log.info { "GroupGen 저장 완료: id=${savedGroupGen.id}, category=${category.title}" }
        return savedGroupGen
    }

    private fun createGroupGenEntity(
        category: Category,
        groupPromptResult: GroupPromptResult,
    ): GroupGen =
        GroupGen(
            category = category.code,
            groupIndices = formatGroupIndices(groupPromptResult.group.group),
            headline = groupPromptResult.groupHeadline.headline,
            summary = groupPromptResult.groupSummary.summary,
            highlightTexts = formatHighlightTexts(groupPromptResult.groupHighlights.highlightTexts),
            groupSourceHeadlines = groupPromptResult.groupSourceHeadlines,
        )

    private fun formatGroupIndices(indices: List<Int>): String = indices.joinToString(prefix = "[", postfix = "]")

    private fun formatHighlightTexts(highlightTexts: List<String>): String = highlightTexts.joinToString(prefix = "[", postfix = "]")
}
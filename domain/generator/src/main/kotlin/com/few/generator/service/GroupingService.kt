package com.few.generator.service

import com.few.generator.config.GroupingProperties
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Group
import com.few.generator.domain.Category
import com.few.generator.domain.vo.GenDetail
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class GroupingService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    private val groupingProperties: GroupingProperties,
) {
    private val log = KotlinLogging.logger {}

    fun performGrouping(
        genDetails: List<GenDetail>,
        category: Category,
    ): Group {
        log.info { "그룹화 시작: ${genDetails.size}개 GenDetail 처리" }

        // 그룹화 수행 (설정에서 타겟 비율 사용)
        val groupPrompt = promptGenerator.toCombinedGroupingPrompt(genDetails, groupingProperties.targetPercentage)
        val group: Group =
            chatGpt.ask(groupPrompt) as? Group
                ?: throw IllegalStateException("ChatGPT 응답을 Group으로 변환할 수 없습니다")

        validateGroupResult(group, category)
        return group
    }

    fun validateGroupSize(group: Group): Group? {
        if (group.group.isEmpty()) {
            log.warn { "그룹화 결과가 비어있습니다" }
            return null
        }

        if (group.group.size < groupingProperties.minGroupSize) {
            log.warn { "그룹화 결과(${group.group.size}개)가 최소 그룹 크기(${groupingProperties.minGroupSize})보다 작습니다" }
            return null
        }

        if (group.group.size > groupingProperties.maxGroupSize) {
            log.warn { "그룹화 결과(${group.group.size}개)가 최대 그룹 크기(${groupingProperties.maxGroupSize})를 초과하여 잘라냅니다" }
            val trimmedGroup = Group(group.group.take(groupingProperties.maxGroupSize))
            log.info { "그룹화 완료: ${trimmedGroup.group.size}개 뉴스 선택됨 (${group.group.size}개에서 조정)" }
            return trimmedGroup
        }

        log.info { "그룹화 완료: ${group.group.size}개 뉴스 선택됨" }
        return group
    }

    private fun validateGroupResult(
        group: Group,
        category: Category,
    ) {
        if (group.group.isEmpty()) {
            log.warn { "카테고리 ${category.title}에 대한 그룹화 결과가 비어있습니다" }
        } else {
            log.info { "카테고리 ${category.title}에 대한 그룹화 완료: ${group.group.size}개 선택됨" }
        }
    }
}
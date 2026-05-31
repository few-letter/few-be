package com.few.generator.service.specifics.groupgen

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.config.GeneratorGsonConfig
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Group
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.domain.GroupGen
import com.few.generator.domain.vo.GroupSourceHeadline
import com.few.generator.repository.GroupGenRepository
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class GroupContentGenerator(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    private val groupGenRepository: GroupGenRepository,
    @Qualifier(GeneratorGsonConfig.Companion.GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun generateGroupContent(
        category: Category,
        gens: List<Gen>,
        group: Group,
        region: Region,
    ): GroupGen {
        log.info { "그룹 콘텐츠 생성 시작: ${group.group.size}개 뉴스" }

        val selectedGenIndices = group.group.map { aiIndex -> aiIndex - 1 }.toSet()
        val validIndices = selectedGenIndices.filter { it >= 0 && it < gens.size }
        if (validIndices.size != selectedGenIndices.size) {
            val invalidIndices = selectedGenIndices - validIndices.toSet()
            log.warn { "유효하지 않은 인덱스 발견: $invalidIndices, 전체=${selectedGenIndices.size}, 유효=${validIndices.size}" }
        }
        val selectedGens = validIndices.map { index -> gens[index] }
        val selectedGenHeadlines = selectedGens.map { it.headline }
        val selectedGenSummaries = selectedGens.map { it.summary }

        val groupHeadline = generateGroupHeadline(selectedGenHeadlines)
        val groupSummary = generateGroupSummary(groupHeadline.headline, selectedGenHeadlines, selectedGenSummaries)
        val groupHighlights = generateGroupHighlights(groupSummary.summary)
        val groupSourceHeadlines = selectedGens.map { GroupSourceHeadline(headline = it.headline, url = it.url) }

        log.info { "그룹 콘텐츠 생성 완료" }

        return groupGenRepository.save(
            GroupGen(
                category = category.code,
                selectedGroupIds = gson.toJson(selectedGens.map { it.id }),
                headline = groupHeadline.headline,
                summary = groupSummary.summary,
                highlightTexts = gson.toJson(groupHighlights.highlightTexts),
                groupSourceHeadlines = gson.toJson(groupSourceHeadlines),
                region = region.code,
            ),
        )
    }

    private fun generateGroupHeadline(selectedGenHeadlines: List<String>): Headline =
        try {
            log.debug { "그룹 헤드라인 생성 시작: ${selectedGenHeadlines.size}개 헤드라인" }
            val result =
                chatGpt.ask(promptGenerator.toGroupHeadlineOnlyPrompt(selectedGenHeadlines)) as? Headline
                    ?: throw IllegalStateException("ChatGPT 응답을 Headline으로 변환할 수 없습니다")
            log.debug { "그룹 헤드라인 생성 완료: ${result.headline}" }
            result
        } catch (e: Exception) {
            log.error(e) { "그룹 헤드라인 생성 실패" }
            throw e
        }

    private fun generateGroupSummary(
        groupHeadline: String,
        selectedGenHeadlines: List<String>,
        selectedGenSummaries: List<String>,
    ): Summary =
        try {
            log.debug { "그룹 요약 생성 시작: ${selectedGenSummaries.size}개 요약" }
            val result =
                chatGpt.ask(
                    promptGenerator.toGroupSummaryWithHeadlinesPrompt(groupHeadline, selectedGenHeadlines, selectedGenSummaries),
                ) as? Summary
                    ?: throw IllegalStateException("ChatGPT 응답을 Summary로 변환할 수 없습니다")
            log.debug { "그룹 요약 생성 완료" }
            result
        } catch (e: Exception) {
            log.error(e) { "그룹 요약 생성 실패" }
            throw e
        }

    private fun generateGroupHighlights(groupSummary: String): HighlightTexts =
        try {
            log.debug { "그룹 하이라이트 생성 시작" }
            val result =
                chatGpt.ask(promptGenerator.toGroupHighlightPrompt(groupSummary)) as? HighlightTexts
                    ?: throw IllegalStateException("ChatGPT 응답을 HighlightTexts로 변환할 수 없습니다")
            log.debug { "그룹 하이라이트 생성 완료: ${result.highlightTexts.size}개" }
            result
        } catch (e: Exception) {
            log.error(e) { "그룹 하이라이트 생성 실패" }
            throw e
        }
}
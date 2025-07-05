package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Group
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Category
import com.few.generator.domain.Gen
import com.few.generator.domain.GroupGen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.vo.GroupSourceHeadline
import com.few.generator.repository.GroupGenRepository
import com.few.generator.repository.RawContentsRepository
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class GroupContentGenerationService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    private val groupGenRepository: GroupGenRepository,
    private val rawContentsRepository: RawContentsRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun generateGroupContent(
        category: Category,
        gens: List<Gen>,
        group: Group,
        provisioningContentsMap: Map<Long, ProvisioningContents>,
    ): GroupGen {
        log.info { "그룹 콘텐츠 생성 시작: ${group.group.size}개 뉴스" }

        // 선택된 Gen들을 이용하여 그룹 헤드라인, 요약, 하이라이트 생성
        // 1-based index를 0-based index로 변환 (AI 모델이 1부터 시작하는 인덱스를 반환)
        val selectedGenIndices = group.group.map { aiIndex -> aiIndex - 1 }.toSet()

        // 인덱스 유효성 검사
        val validIndices = selectedGenIndices.filter { it >= 0 && it < gens.size }
        if (validIndices.size != selectedGenIndices.size) {
            val invalidIndices = selectedGenIndices - validIndices.toSet()
            log.warn { "유효하지 않은 인덱스 발견: $invalidIndices, 전체=${selectedGenIndices.size}, 유효=${validIndices.size}" }
            // 데이터 일관성을 위해 예외 처리 고려 가능
            // throw IllegalArgumentException("유효하지 않은 인덱스가 포함되어 있습니다: $invalidIndices")
        }
        val selectedGens = validIndices.map { index -> gens[index] }
        val selectedGenHeadlines = selectedGens.map { it.headline }
        val selectedGenSummaries = selectedGens.map { it.summary }

        // 그룹 헤드라인 생성
        val groupHeadline = generateGroupHeadline(selectedGenHeadlines)

        // 그룹 요약 생성
        val groupSummary = generateGroupSummary(groupHeadline.headline, selectedGenHeadlines, selectedGenSummaries)

        // 그룹 하이라이트 생성
        val groupHighlights = generateGroupHighlights(groupSummary.summary)

        // 소스 헤드라인 생성 (이미 조회한 데이터 재사용)
        val groupSourceHeadlines = createGroupSourceHeadlines(selectedGens, provisioningContentsMap)

        log.info { "그룹 콘텐츠 생성 완료" }

        val groupGen =
            GroupGen(
                category = category.code,
                groupIndices = gson.toJson(group.group),
                headline = groupHeadline.headline,
                summary = groupSummary.summary,
                highlightTexts = gson.toJson(groupHighlights.highlightTexts),
                groupSourceHeadlines = gson.toJson(groupSourceHeadlines),
            )

        return groupGenRepository.save(groupGen)
    }

    fun createEmptyGroupGen(category: Category): GroupGen =
        groupGenRepository.save(
            GroupGen(
                category = category.code,
                groupIndices = gson.toJson(emptyList<Int>()),
                headline = "",
                summary = "",
                highlightTexts = gson.toJson(emptyList<String>()),
                groupSourceHeadlines = gson.toJson(emptyList<GroupSourceHeadline>()),
            ),
        )

    private fun generateGroupHeadline(selectedGenHeadlines: List<String>): Headline =
        try {
            log.debug { "그룹 헤드라인 생성 시작: ${selectedGenHeadlines.size}개 헤드라인" }
            val groupHeadlinePrompt = promptGenerator.toGroupHeadlineOnlyPrompt(selectedGenHeadlines)
            val result =
                chatGpt.ask(groupHeadlinePrompt) as? Headline
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
            val groupSummaryPrompt =
                promptGenerator.toGroupSummaryWithHeadlinesPrompt(
                    groupHeadline,
                    selectedGenHeadlines,
                    selectedGenSummaries,
                )
            val result =
                chatGpt.ask(groupSummaryPrompt) as? Summary
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
            val groupHighlightPrompt = promptGenerator.toGroupHighlightPrompt(groupSummary)
            val result =
                chatGpt.ask(groupHighlightPrompt) as? HighlightTexts
                    ?: throw IllegalStateException("ChatGPT 응답을 HighlightTexts로 변환할 수 없습니다")
            log.debug { "그룹 하이라이트 생성 완료: ${result.highlightTexts.size}개" }
            result
        } catch (e: Exception) {
            log.error(e) { "그룹 하이라이트 생성 실패" }
            throw e
        }

    private fun createGroupSourceHeadlines(
        selectedGens: List<Gen>,
        provisioningContentsMap: Map<Long, ProvisioningContents>,
    ): List<GroupSourceHeadline> {
        if (selectedGens.isEmpty()) return emptyList()

        try {
            // 이미 조회된 ProvisioningContents에서 RawContents ID 추출
            val rawContentsIds =
                selectedGens
                    .mapNotNull { gen ->
                        provisioningContentsMap[gen.provisioningContentsId]?.rawContentsId
                    }.distinct()

            // RawContents만 배치 조회
            val rawContentsMap =
                rawContentsRepository
                    .findAllByIdIn(rawContentsIds)
                    .associateBy { it.id!! }

            return selectedGens.mapNotNull { gen ->
                try {
                    val provisioningContent = provisioningContentsMap[gen.provisioningContentsId] ?: return@mapNotNull null
                    val rawContent = rawContentsMap[provisioningContent.rawContentsId] ?: return@mapNotNull null

                    GroupSourceHeadline(
                        headline = gen.headline,
                        url = rawContent.url,
                    )
                } catch (e: Exception) {
                    log.warn(e) { "GroupSourceHeadline 생성 실패: Gen ID ${gen.id}" }
                    null
                }
            }
        } catch (e: Exception) {
            log.error(e) { "GroupSourceHeadlines 생성 실패, 캐시된 데이터 사용" }
            return emptyList()
        }
    }
}
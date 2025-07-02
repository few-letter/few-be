package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.prompt.schema.Group
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.domain.vo.GroupSourceHeadline
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.repository.RawContentsRepository
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

data class GroupPromptResult(
    val group: Group,
    val groupHeadline: Headline,
    val groupSummary: Summary,
    val groupHighlights: HighlightTexts,
    val groupSourceHeadlines: List<GroupSourceHeadline> = emptyList(),
)

@Service
class GroupGenerationService(
    private val groupPromptService: GroupPromptService,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val rawContentsRepository: RawContentsRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun generateGroupContent(gens: List<Gen>): GroupPromptResult {
        if (gens.isEmpty()) {
            throw IllegalArgumentException("Gens list cannot be empty")
        }

        log.info { "그룹 콘텐츠 생성 시작. Gen 개수: ${gens.size}" }

        val groupPromptResult = runGroupPromptsInternal(gens)

        log.info { "그룹 콘텐츠 생성 완료" }
        return groupPromptResult
    }

    private fun runGroupPromptsInternal(gens: List<Gen>): GroupPromptResult {
        val webGenDetails =
            gens.map { gen ->
                val keywordsJson = extractKeywordsFromGen(gen)
                gen.headline to keywordsJson
            }

        val group = groupPromptService.groupWebGen(webGenDetails)
        if (group.group.isEmpty()) {
            log.warn { "그룹화 결과가 비어있습니다" }
            return createEmptyGroupResult()
        }

        val selectedGens = group.group.map { index -> gens[index - 1] }
        val headlines = selectedGens.map { it.headline }
        val summaries = selectedGens.map { it.summary }

        val groupHeadline = groupPromptService.groupHeadline(headlines)
        val groupSummary =
            groupPromptService.groupSummary(
                groupHeadline.headline,
                headlines,
                summaries,
            )
        val groupHighlights = groupPromptService.groupHighlights(groupSummary.summary)

        val groupSourceHeadlines = createGroupSourceHeadlines(selectedGens)

        return GroupPromptResult(
            group = group,
            groupHeadline = groupHeadline,
            groupSummary = groupSummary,
            groupHighlights = groupHighlights,
            groupSourceHeadlines = groupSourceHeadlines,
        )
    }

    private fun extractKeywordsFromGen(gen: Gen): String {
        return try {
            val provisioningContent =
                provisioningContentsRepository
                    .findById(gen.provisioningContentsId)
                    .orElse(null) ?: return "키워드 없음"

            val rawContent =
                rawContentsRepository
                    .findById(provisioningContent.rawContentsId)
                    .orElse(null) ?: return "키워드 없음"

            rawContent.url
        } catch (e: Exception) {
            log.warn(e) { "키워드 추출 실패: Gen ID ${gen.id}" }
            "키워드 없음"
        }
    }

    private fun createGroupSourceHeadlines(selectedGens: List<Gen>): List<GroupSourceHeadline> {
        return selectedGens.mapNotNull { gen ->
            try {
                val provisioningContent =
                    provisioningContentsRepository
                        .findById(gen.provisioningContentsId)
                        .orElse(null) ?: return@mapNotNull null

                val rawContent =
                    rawContentsRepository
                        .findById(provisioningContent.rawContentsId)
                        .orElse(null) ?: return@mapNotNull null

                GroupSourceHeadline(
                    headline = gen.headline,
                    url = rawContent.url,
                )
            } catch (e: Exception) {
                log.warn(e) { "GroupSourceHeadline 생성 실패: Gen ID ${gen.id}" }
                null
            }
        }
    }

    private fun createEmptyGroupResult(): GroupPromptResult =
        GroupPromptResult(
            group = Group(emptyList()),
            groupHeadline = Headline(""),
            groupSummary = Summary(""),
            groupHighlights = HighlightTexts(emptyList()),
            groupSourceHeadlines = emptyList(),
        )

    private fun Long.msToSeconds(): Double = this / 1000.0
}
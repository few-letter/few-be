package com.few.generator.service

import com.few.generator.core.gpt.prompt.schema.Group
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.domain.vo.GroupSourceHeadline
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.repository.RawContentsRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

data class GroupContentResult(
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
    private val keyWordsService: KeyWordsService,
) {
    private val log = KotlinLogging.logger {}

    fun generateGroupContent(gens: List<Gen>): GroupContentResult {
        if (gens.isEmpty()) {
            throw IllegalArgumentException("Gens list cannot be empty")
        }
        log.info { "그룹 콘텐츠 생성 시작. Gen 개수: ${gens.size}" }

        val groupContent = doGenerateGroupContent(gens)

        log.info { "그룹 콘텐츠 생성 완료" }
        return groupContent
    }

    private fun doGenerateGroupContent(gens: List<Gen>): GroupContentResult {
        val genDetails =
            gens.map { gen ->
                val coreTexts =
                    provisioningContentsRepository
                        .findById(gen.provisioningContentsId)
                        .getOrNull()
                        ?.coreTextsJson ?: "키워드 없음"

                val keyWords = keyWordsService.generateKeyWords(coreTexts)
                gen.headline to keyWords
            }

        // headline과 keyWords를 사용하여 그룹화
        // genDetails의 인덱스에 +1 한 값을 그룹화된 결과로 사용
        val groupGen = groupPromptService.groupGen(genDetails)
        val group = groupGen.group
        if (group.isEmpty()) {
            log.warn { "그룹화 결과가 비어있습니다" }
            return GroupContentResult(
                group = Group(emptyList()),
                groupHeadline = Headline(""),
                groupSummary = Summary(""),
                groupHighlights = HighlightTexts(emptyList()),
                groupSourceHeadlines = emptyList(),
            )
        }

        // 선택된 Gen들을 이용하여 이후 그룹 헤드라인, 요약, 하이라이트 생성
        val selectedGenIndex = group.map { it - 1 }.toSet()
        val selectedGens = selectedGenIndex.map { index -> gens[index - 1] }
        val selectedGenHeadlines = selectedGens.map { it.headline }
        val selectedGenSummaries = selectedGens.map { it.summary }

        val groupHeadline = groupPromptService.groupHeadline(selectedGenHeadlines)
        val groupSummary = groupPromptService.groupSummary(groupHeadline.headline, selectedGenHeadlines, selectedGenSummaries)
        val groupHighlights = groupPromptService.groupHighlights(groupSummary.summary)
        val groupSourceHeadlines = createGroupSourceHeadlines(selectedGens)

        return GroupContentResult(
            group = groupGen,
            groupHeadline = groupHeadline,
            groupSummary = groupSummary,
            groupHighlights = groupHighlights,
            groupSourceHeadlines = groupSourceHeadlines,
        )
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
}
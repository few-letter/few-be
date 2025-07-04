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
import com.few.generator.repository.GenRepository
import com.few.generator.repository.GroupGenRepository
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.repository.RawContentsRepository
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class GroupGenService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    private val genRepository: GenRepository,
    private val groupGenRepository: GroupGenRepository,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val rawContentsRepository: RawContentsRepository,
    private val keyWordsService: KeyWordsService,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun createGroupGen(category: Category): GroupGen {
        log.info { "그룹 생성 시작: category=${category.title}" }

        val timeRange = getTodayTimeRange()
        val gens =
            genRepository.findAllByCreatedAtBetweenAndCategory(
                timeRange.first,
                timeRange.second,
                category.code,
            )

        if (gens.isEmpty()) {
            log.warn { "카테고리 ${category.title}에 대한 Gen이 없습니다." }
            return createEmptyGroupGen(category)
        }

        log.info { "카테고리 ${category.title}에서 ${gens.size}개 Gen 발견, 키워드 추출 시작" }

        // 배치로 ProvisioningContents 조회하여 N+1 쿼리 방지
        val provisioningContentsIds = gens.map { it.provisioningContentsId }
        val provisioningContentsMap =
            provisioningContentsRepository
                .findAllByIdIn(provisioningContentsIds)
                .associateBy { it.id!! }

        // 비동기로 키워드 추출 시작
        val keyWordsFutures =
            gens.map { gen ->
                val coreTexts =
                    provisioningContentsMap[gen.provisioningContentsId]
                        ?.coreTextsJson ?: "키워드 없음"

                gen to keyWordsService.generateKeyWordsAsync(coreTexts)
            }

        // 모든 비동기 키워드 추출 완료 대기
        val genDetails =
            keyWordsFutures.map { (gen, future) ->
                val keyWords = future.get() // 비동기 결과 대기
                log.debug { "Gen ${gen.id} 키워드 추출 완료: $keyWords" }

                gen.headline to keyWords
            }

        log.info { "키워드 추출 완료, 그룹화 시작" }

        // 그룹화 수행
        val groupPrompt = promptGenerator.toCombinedGroupingPrompt(genDetails, 30)
        val group: Group = chatGpt.ask(groupPrompt) as Group

        if (group.group.isEmpty()) {
            log.warn { "그룹화 결과가 비어있습니다" }
            return createEmptyGroupGen(category)
        }

        log.info { "그룹화 완료: ${group.group.size}개 뉴스 선택됨" }

        // 선택된 Gen들을 이용하여 그룹 헤드라인, 요약, 하이라이트 생성
        val selectedGenIndices = group.group.map { it - 1 }.toSet()
        val selectedGens = selectedGenIndices.map { index -> gens[index] }
        val selectedGenHeadlines = selectedGens.map { it.headline }
        val selectedGenSummaries = selectedGens.map { it.summary }

        // 그룹 헤드라인 생성
        val groupHeadlinePrompt = promptGenerator.toGroupHeadlineOnlyPrompt(selectedGenHeadlines)
        val groupHeadline: Headline = chatGpt.ask(groupHeadlinePrompt) as Headline

        // 그룹 요약 생성
        val groupSummaryPrompt =
            promptGenerator.toGroupSummaryWithHeadlinesPrompt(
                groupHeadline.headline,
                selectedGenHeadlines,
                selectedGenSummaries,
            )
        val groupSummary: Summary = chatGpt.ask(groupSummaryPrompt) as Summary

        // 그룹 하이라이트 생성
        val groupHighlightPrompt = promptGenerator.toGroupHighlightPrompt(groupSummary.summary)
        val groupHighlights: HighlightTexts = chatGpt.ask(groupHighlightPrompt) as HighlightTexts

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

    private fun getTodayTimeRange(): Pair<LocalDateTime, LocalDateTime> {
        val start =
            LocalDateTime
                .now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
        val end =
            LocalDateTime
                .now()
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
        return start to end
    }

    private fun createEmptyGroupGen(category: Category): GroupGen =
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
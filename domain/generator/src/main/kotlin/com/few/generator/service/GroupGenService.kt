package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.config.GroupingProperties
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
import com.few.generator.domain.vo.AsyncKeywordExtraction
import com.few.generator.domain.vo.DateTimeRange
import com.few.generator.domain.vo.GenDetail
import com.few.generator.domain.vo.GroupGenProcessingResult
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
import kotlin.system.measureTimeMillis

@Service
class GroupGenService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    private val genRepository: GenRepository,
    private val groupGenRepository: GroupGenRepository,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val rawContentsRepository: RawContentsRepository,
    private val keyWordsService: KeyWordsService,
    private val groupingProperties: GroupingProperties,
    private val groupGenMetricsService: GroupGenMetricsService,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun createGroupGen(category: Category): GroupGen {
        log.info { "그룹 생성 시작: category=${category.title}" }

        var result: GroupGen? = null
        var keywordExtractionTime: Long = 0
        var totalGens: Int = 0
        val totalProcessingTime =
            measureTimeMillis {
                try {
                    val internalResult = createGroupGenInternalWithMetrics(category)
                    result = internalResult.groupGen
                    keywordExtractionTime = internalResult.keywordExtractionTime
                    totalGens = internalResult.totalGens
                } catch (e: Exception) {
                    log.error(e) { "그룹 생성 중 오류 발생: category=${category.title}" }
                    result = createEmptyGroupGen(category)
                }
            }

        // 전체 처리 시간이 측정된 후 메트릭 기록
        val finalResult = result
        if (finalResult == null || (finalResult.headline.isEmpty() && finalResult.summary.isEmpty())) {
            groupGenMetricsService.recordGroupGenError(category, "GroupGen creation failed", totalProcessingTime)
        } else {
            // 성공 시 전체 처리 시간 포함한 메트릭 업데이트
            updateSuccessMetrics(category, finalResult, keywordExtractionTime, totalProcessingTime, totalGens)
        }

        return result ?: createEmptyGroupGen(category)
    }

    private fun createGroupGenInternalWithMetrics(category: Category): GroupGenProcessingResult {
        val timeRange = getTodayTimeRange()
        val gens =
            genRepository.findAllByCreatedAtBetweenAndCategory(
                timeRange.startTime,
                timeRange.endTime,
                category.code,
            )

        if (gens.isEmpty()) {
            log.warn { "카테고리 ${category.title}에 대한 Gen이 없습니다." }
            return GroupGenProcessingResult(createEmptyGroupGen(category), 0, 0)
        }

        if (gens.size < groupingProperties.minGroupSize) {
            log.warn { "카테고리 ${category.title}의 Gen 개수(${gens.size})가 최소 그룹 크기(${groupingProperties.minGroupSize})보다 작습니다." }
            return GroupGenProcessingResult(createEmptyGroupGen(category), 0, gens.size)
        }

        log.info { "카테고리 ${category.title}에서 ${gens.size}개 Gen 발견, 키워드 추출 시작" }

        // 배치로 ProvisioningContents 조회하여 N+1 쿼리 방지
        val provisioningContentsIds = gens.map { it.provisioningContentsId }
        val provisioningContentsMap =
            provisioningContentsRepository
                .findAllByIdIn(provisioningContentsIds)
                .associateBy { it.id!! }

        // 키워드 추출 시간 측정 및 실행
        val genDetails: List<GenDetail>
        val keywordExtractionTime =
            measureTimeMillis {
                // 비동기로 키워드 추출 시작
                val keyWordsFutures =
                    gens.map { gen ->
                        val coreTexts =
                            provisioningContentsMap[gen.provisioningContentsId]
                                ?.coreTextsJson ?: "키워드 없음"

                        AsyncKeywordExtraction(
                            gen = gen,
                            keywordFuture = keyWordsService.generateKeyWordsAsync(coreTexts),
                        )
                    }

                // 모든 비동기 키워드 추출 완료 대기
                genDetails =
                    keyWordsFutures.map { extraction ->
                        val keyWords = extraction.keywordFuture.get() // 비동기 결과 대기
                        log.debug { "Gen ${extraction.gen.id} 키워드 추출 완료: $keyWords" }

                        GenDetail(
                            headline = extraction.gen.headline,
                            keywords = keyWords,
                        )
                    }
            }

        log.info { "키워드 추출 완료, 그룹화 시작" }

        // 그룹화 수행 (설정에서 타겟 비율 사용)
        val groupPrompt = promptGenerator.toCombinedGroupingPrompt(genDetails, groupingProperties.targetPercentage)
        val group: Group = chatGpt.ask(groupPrompt) as Group

        if (group.group.isEmpty()) {
            log.warn { "그룹화 결과가 비어있습니다" }
            return GroupGenProcessingResult(createEmptyGroupGen(category), keywordExtractionTime, gens.size)
        }

        if (group.group.size < groupingProperties.minGroupSize) {
            log.warn { "그룹화 결과(${group.group.size}개)가 최소 그룹 크기(${groupingProperties.minGroupSize})보다 작습니다" }
            return GroupGenProcessingResult(createEmptyGroupGen(category), keywordExtractionTime, gens.size)
        }

        if (group.group.size > groupingProperties.maxGroupSize) {
            log.warn { "그룹화 결과(${group.group.size}개)가 최대 그룹 크기(${groupingProperties.maxGroupSize})를 초과하여 잘라냅니다" }
            val trimmedGroup = Group(group.group.take(groupingProperties.maxGroupSize))
            log.info { "그룹화 완료: ${trimmedGroup.group.size}개 뉴스 선택됨 (${group.group.size}개에서 조정)" }

            // 잘린 그룹으로 계속 진행
            val result = generateGroupContent(category, gens, trimmedGroup, provisioningContentsMap)
            return GroupGenProcessingResult(result, keywordExtractionTime, gens.size)
        }

        log.info { "그룹화 완료: ${group.group.size}개 뉴스 선택됨" }
        val result = generateGroupContent(category, gens, group, provisioningContentsMap)
        return GroupGenProcessingResult(result, keywordExtractionTime, gens.size)
    }

    private fun updateSuccessMetrics(
        category: Category,
        groupGen: GroupGen,
        keywordExtractionTime: Long,
        totalProcessingTime: Long,
        totalGens: Int,
    ) {
        try {
            // GroupGen에서 선택된 Gen 개수 추출
            val groupIndices = gson.fromJson(groupGen.groupIndices, Array<Int>::class.java)?.toList() ?: emptyList()

            groupGenMetricsService.recordGroupGenMetrics(
                GroupGenMetricsService.GroupGenMetrics(
                    category = category,
                    totalGens = totalGens,
                    selectedGens = groupIndices.size,
                    groupingSuccessful = true,
                    keywordExtractionTimeMs = keywordExtractionTime,
                    totalProcessingTimeMs = totalProcessingTime,
                    errorMessage = null,
                ),
            )
        } catch (e: Exception) {
            log.warn(e) { "성공 메트릭 기록 실패" }
        }
    }

    private fun generateGroupContent(
        category: Category,
        gens: List<Gen>,
        group: Group,
        provisioningContentsMap: Map<Long, ProvisioningContents>,
    ): GroupGen {
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

        // 카테고리 정보는 파라미터로 전달받음

        val groupGen =
            GroupGen(
                category = category.code,
                groupIndices = gson.toJson(group.group),
                headline = groupHeadline.headline,
                summary = groupSummary.summary,
                highlightTexts = gson.toJson(groupHighlights.highlightTexts),
                groupSourceHeadlines = gson.toJson(groupSourceHeadlines),
            )

        val savedGroupGen = groupGenRepository.save(groupGen)

        // 성공 메트릭은 상위 메서드 updateSuccessMetrics에서 처리

        return savedGroupGen
    }

    private fun getTodayTimeRange(): DateTimeRange {
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
        return DateTimeRange(startTime = start, endTime = end)
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
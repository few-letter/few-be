package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.config.GroupingProperties
import com.few.generator.domain.Category
import com.few.generator.domain.GroupGen
import com.few.generator.domain.vo.GenDetail
import com.few.generator.domain.vo.GroupGenProcessingResult
import com.few.generator.repository.GenRepository
import com.few.generator.repository.ProvisioningContentsRepository
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class GroupGenService(
    private val genRepository: GenRepository,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val groupingProperties: GroupingProperties,
    private val groupGenMetricsService: GroupGenMetricsService,
    private val keywordExtractionService: KeywordExtractionService,
    private val groupingService: GroupingService,
    private val groupContentGenerationService: GroupContentGenerationService,
    private val dateTimeService: DateTimeService,
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
                    val internalResult =
                        runBlocking {
                            createGroupGenInternalWithMetrics(category)
                        }
                    result = internalResult.groupGen
                    keywordExtractionTime = internalResult.keywordExtractionTime
                    totalGens = internalResult.totalGens
                } catch (e: Exception) {
                    log.error(e) { "그룹 생성 중 오류 발생: category=${category.title}" }
                    result = groupContentGenerationService.createEmptyGroupGen(category)
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

        return result ?: groupContentGenerationService.createEmptyGroupGen(category)
    }

    private suspend fun createGroupGenInternalWithMetrics(category: Category): GroupGenProcessingResult {
        val timeRange = dateTimeService.getTodayTimeRange()
        val gens =
            genRepository.findAllByCreatedAtBetweenAndCategory(
                timeRange.startTime,
                timeRange.endTime,
                category.code,
            )

        if (gens.isEmpty()) {
            log.warn { "카테고리 ${category.title}에 대한 Gen이 없습니다." }
            return GroupGenProcessingResult(groupContentGenerationService.createEmptyGroupGen(category), 0, 0)
        }

        if (gens.size < groupingProperties.minGroupSize) {
            log.warn { "카테고리 ${category.title}의 Gen 개수(${gens.size})가 최소 그룹 크기(${groupingProperties.minGroupSize})보다 작습니다." }
            return GroupGenProcessingResult(groupContentGenerationService.createEmptyGroupGen(category), 0, gens.size)
        }

        log.info { "카테고리 ${category.title}에서 ${gens.size}개 Gen 발견, 키워드 추출 시작" }

        // 배치로 ProvisioningContents 조회하여 N+1 쿼리 방지
        val provisioningContentsIds = gens.map { it.provisioningContentsId }
        val provisioningContentsMap =
            provisioningContentsRepository
                .findAllByIdIn(provisioningContentsIds)
                .associateBy { it.id!! }

        // 키워드 추출 시간 측정 및 실행 (코루틴 버전)
        val genDetails: List<GenDetail>
        val keywordExtractionTime =
            measureTimeMillis {
                genDetails = keywordExtractionService.extractKeywordsFromGens(gens, provisioningContentsMap)
            }

        log.info { "키워드 추출 완료, 그룹화 시작" }

        // 그룹화 수행
        val group = groupingService.performGrouping(genDetails, category)
        val validatedGroup = groupingService.validateGroupSize(group)

        if (validatedGroup == null) {
            return GroupGenProcessingResult(groupContentGenerationService.createEmptyGroupGen(category), keywordExtractionTime, gens.size)
        }

        // 그룹 콘텐츠 생성
        val result = groupContentGenerationService.generateGroupContent(category, gens, validatedGroup, provisioningContentsMap)
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
}
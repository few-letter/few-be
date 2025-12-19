package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.common.exception.BadRequestException
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.config.GroupingProperties
import com.few.generator.domain.GroupGen
import com.few.generator.domain.vo.GenDetail
import com.few.generator.domain.vo.GroupGenProcessingResult
import com.few.generator.event.dto.ContentsSchedulingEventDto
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.specifics.groupgen.GenGroupper
import com.few.generator.service.specifics.groupgen.GroupContentGenerator
import com.few.generator.service.specifics.groupgen.GroupGenMetrics
import com.few.generator.service.specifics.groupgen.KeywordExtractor
import com.few.generator.support.jpa.GeneratorTransactional
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Propagation
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

abstract class AbstractGroupGenSchedulingUseCase(
    protected val applicationEventPublisher: ApplicationEventPublisher,
    protected val genService: GenService,
    protected val provisioningService: ProvisioningService,
    protected val groupingProperties: GroupingProperties,
    @Qualifier(GSON_BEAN_NAME)
    protected val gson: Gson,
    protected val groupGenMetrics: GroupGenMetrics,
    protected val keywordExtractor: KeywordExtractor,
    protected val genGrouper: GenGroupper,
    protected val groupContentGenerator: GroupContentGenerator,
) {
    protected val log = KotlinLogging.logger {}
    protected val isRunning = AtomicBoolean(false)
    protected val groupGenScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    abstract val region: Region
    abstract val regionName: String
    abstract val eventTitle: String

    @GeneratorTransactional(propagation = Propagation.REQUIRED)
    protected open fun execute() {
        if (!isRunning.compareAndSet(false, true)) {
            throw BadRequestException("$regionName group scheduling is already running. Please try again later.")
        }

        try {
            doExecute()
        } finally {
            isRunning.set(false)
        }
    }

    protected fun doExecute() {
        val startTime = LocalDateTime.now()
        var isSuccess = true
        var creationTimeSec = 0.0
        var exception: Throwable? = null
        var successCnt = 0

        runCatching {
            creationTimeSec =
                measureTimeMillis {
                    successCnt = createGroupGens().size
                }.msToSeconds()
        }.onFailure { ex ->
            isSuccess = false
            log.error(ex) { "$regionName 그룹 스케줄링 중 오류 발생" }
            exception = ex
        }.also {
            log.info {
                buildString {
                    appendLine("✅ $regionName Group Scheduling Result")
                    appendLine("✅ isSuccess: $isSuccess")
                    appendLine("✅ 시작 시간: $startTime")
                    appendLine("✅ 소요 시간: $creationTimeSec")
                    appendLine("✅ message: ${exception?.cause?.message}")
                    append("✅ result: 생성된 그룹 수: $successCnt")
                }
            }

            applicationEventPublisher.publishEvent(
                ContentsSchedulingEventDto(
                    title = eventTitle,
                    isSuccess = isSuccess,
                    startTime = startTime,
                    totalTime = "%.3f".format(creationTimeSec),
                    message = if (isSuccess) "None" else exception?.cause?.message ?: "Unknown error",
                    result = if (isSuccess) "생성($successCnt)" else "None",
                ),
            )

            if (!isSuccess) {
                throw BadRequestException("$regionName 그룹 스케줄링에 실패 : ${exception?.cause?.message}")
            }
        }
    }

    fun createGroupGens(): List<GroupGen> {
        log.info { "$regionName 전체 카테고리 그룹 생성 시작" }

        val results = mutableListOf<GroupGen>()
        val categories = Category.groupGenEntries()

        categories.forEach { category ->
            try {
                val groupGen = createGroupGen(category)
                results.add(groupGen)
                log.info { "$regionName 카테고리 ${category.title} 그룹 생성 완료" }
            } catch (e: Exception) {
                log.error(e) { "$regionName 카테고리 ${category.title} 그룹 생성 실패하여 Skip" }
            }
        }

        log.info { "$regionName 전체 카테고리 그룹 생성 완료: ${results.size}개" }
        return results
    }

    fun createGroupGen(category: Category): GroupGen {
        log.info { "$regionName 그룹 생성 시작: category=${category.title}" }

        var result: GroupGen? = null
        var keywordExtractionTime: Long = 0
        var totalGens: Int = 0
        var processingError: Exception? = null

        val totalProcessingTime =
            measureTimeMillis {
                try {
                    val internalResult =
                        runBlocking(groupGenScope.coroutineContext) {
                            createGroupGenInternalWithMetrics(category)
                        }
                    result = internalResult.groupGen
                    keywordExtractionTime = internalResult.keywordExtractionTime
                    totalGens = internalResult.totalGens
                } catch (e: Exception) {
                    log.error(e) { "$regionName 그룹 생성 중 오류 발생: category=${category.title}, 전체 Gen 수: $totalGens" }
                    processingError = e
                }
            }

        // 처리 중 에러가 발생한 경우 메트릭 기록
        processingError?.let { error ->
            groupGenMetrics.recordGroupGenError(category, error.message ?: "Unknown error", totalProcessingTime)
        }

        // 전체 처리 시간이 측정된 후 메트릭 기록
        val finalResult = result
        if (finalResult == null || (finalResult.headline.isEmpty() && finalResult.summary.isEmpty())) {
            log.warn {
                "$regionName 그룹 생성 실패 또는 빈 결과: category=${category.title}, headline=${finalResult?.headline?.isNotEmpty()}, summary=${finalResult?.summary?.isNotEmpty()}"
            }
            groupGenMetrics.recordGroupGenError(category, "GroupGen creation failed", totalProcessingTime)
        } else {
            // 성공 시 전체 처리 시간 포함한 메트릭 업데이트
            updateSuccessMetrics(category, finalResult, keywordExtractionTime, totalProcessingTime, totalGens)
        }

        return result ?: throw BadRequestException("$regionName Group Gen 생성 실패 - Cause: Unknown (카테고리: ${category.title})")
    }

    private suspend fun createGroupGenInternalWithMetrics(category: Category): GroupGenProcessingResult {
        val gens =
            genService.findAllByCreatedAtBetweenAndCategoryAndRegion(
                category,
                region,
            )

        if (gens.isEmpty()) {
            throw BadRequestException("$regionName Group Gen 생성 실패 - Cause: 카테고리 ${category.title}에 대한 Gen이 없습니다.")
        }

        if (gens.size < groupingProperties.minGroupSize) {
            throw BadRequestException(
                "$regionName Group Gen 생성 실패 - Cause: 카테고리 ${category.title}의 Gen 개수(${gens.size})가 최소 그룹 크기(${groupingProperties.minGroupSize})보다 작습니다.",
            )
        }

        log.info { "$regionName 카테고리 ${category.title}에서 ${gens.size}개 Gen 발견, 키워드 추출 시작" }

        // 배치로 ProvisioningContents 조회하여 N+1 쿼리 방지
        val provisioningContentsIds = gens.map { it.provisioningContentsId }
        val provisioningContentsMap =
            provisioningService
                .findAllByIdIn(provisioningContentsIds)
                .associateBy { it.id!! }

        // 키워드 추출 시간 측정 및 실행 (코루틴 버전)
        val genDetails: List<GenDetail>
        val keywordExtractionTime =
            measureTimeMillis {
                genDetails = keywordExtractor.extractKeywordsFromGens(gens, provisioningContentsMap)
            }

        log.info { "키워드 추출 완료, 그룹화 시작" }

        // 그룹화 수행
        val group = genGrouper.performGrouping(genDetails, category)
        val validatedGroup = genGrouper.validateGroupSize(group)

        if (validatedGroup == null) {
            throw BadRequestException("$regionName Group Gen 생성 실패 - Cause: 카테고리 ${category.title} Gen Grouping 실패")
        }

        // 그룹 콘텐츠 생성
        val result = groupContentGenerator.generateGroupContent(category, gens, validatedGroup, provisioningContentsMap, region)
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
            val groupIndices =
                try {
                    gson.fromJson(groupGen.selectedGroupIds, Array<Int>::class.java)?.toList() ?: emptyList()
                } catch (e: Exception) {
                    log.warn(e) { "GroupIndices JSON 파싱 실패: ${groupGen.selectedGroupIds}" }
                    emptyList()
                }

            groupGenMetrics.recordGroupGenMetrics(
                GroupGenMetrics.GroupGenMetrics(
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

    protected fun Long.msToSeconds(): Double = this / 1000.0
}
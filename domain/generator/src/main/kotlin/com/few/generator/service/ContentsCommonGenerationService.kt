package com.few.generator.service

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.common.exception.BadRequestException
import com.few.generator.config.GroupingProperties
import com.few.generator.domain.vo.GenDetail
import com.few.generator.domain.vo.GroupGenProcessingResult
import com.few.generator.service.specifics.groupgen.GenGroupper
import com.few.generator.service.specifics.groupgen.GroupContentGenerator
import com.few.generator.service.specifics.groupgen.KeywordExtractor
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class ContentsCommonGenerationService(
    protected val rawContentsService: RawContentsService,
    protected val provisioningService: ProvisioningService,
    protected val genService: GenService,
    protected val applicationEventPublisher: ApplicationEventPublisher,
    protected val groupingProperties: GroupingProperties,
    protected val keywordExtractor: KeywordExtractor,
    protected val genGrouper: GenGroupper,
    protected val groupContentGenerator: GroupContentGenerator,
) {
    protected val log = KotlinLogging.logger {}

    /**
     * RawContents, ProvisioningContents, Gen 중 1개라도 실패시 rollback하기 위해
     * 개별 트랜잭션으로 분리
     */
    @GeneratorTransactional
    open fun createSingleContents(
        url: String,
        category: Category,
        region: Region,
    ) {
        val rawContent = rawContentsService.createAndSave(url, category, region)
        val provisioningContent = provisioningService.createAndSave(rawContent)
        genService.createAndSave(rawContent, provisioningContent)
    }

    @GeneratorTransactional
    open suspend fun createSingleGroupGen(
        category: Category,
        region: Region,
    ): GroupGenProcessingResult {
        val gens =
            genService.findAllByCreatedAtTodayAndCategoryAndRegion(
                category,
                region,
            )

        if (gens.isEmpty()) {
            throw BadRequestException("${region.name} Group Gen 생성 실패 - Cause: 카테고리 ${category.title}에 대한 Gen이 없습니다.")
        }

        if (gens.size < groupingProperties.minGroupSize) {
            throw BadRequestException(
                "${region.name} Group Gen 생성 실패 - Cause: 카테고리 ${category.title}의 Gen 개수(${gens.size})가 최소 그룹 크기(${groupingProperties.minGroupSize})보다 작습니다.",
            )
        }

        log.info { "${region.name} 카테고리 ${category.title}에서 ${gens.size}개 Gen 발견, 키워드 추출 시작" }

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
            throw BadRequestException("${region.name} Group Gen 생성 실패 - Cause: 카테고리 ${category.title} Gen Grouping 실패")
        }

        // 그룹 콘텐츠 생성
        val result = groupContentGenerator.generateGroupContent(category, gens, validatedGroup, provisioningContentsMap, region)
        return GroupGenProcessingResult(result, keywordExtractionTime, gens.size)
    }
}
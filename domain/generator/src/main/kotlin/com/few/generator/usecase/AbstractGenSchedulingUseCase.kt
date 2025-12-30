package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.common.exception.BadRequestException
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.domain.Gen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.event.ContentsSchedulingEvent
import com.few.generator.event.GenSchedulingCompletedEvent
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

abstract class AbstractGenSchedulingUseCase(
    protected val rawContentsService: RawContentsService,
    protected val provisioningService: ProvisioningService,
    protected val genService: GenService,
    protected val applicationEventPublisher: ApplicationEventPublisher,
    protected val scrapper: Scrapper,
    @Value("\${generator.contents.countByCategory}")
    protected val contentsCountByCategory: Int,
) {
    protected val log = KotlinLogging.logger {}
    protected val isRunning = AtomicBoolean(false)

    abstract val region: Region
    abstract val regionName: String
    abstract val schedulingName: String
    abstract val eventTitle: String

    @GeneratorTransactional
    protected open fun execute() {
        // 0~15분 사이 랜덤으로 sleep 후 진행
//        Thread.sleep((0..15).random().toLong() * 60 * 1000)

        if (!isRunning.compareAndSet(false, true)) {
            throw BadRequestException("$schedulingName is already running. Please try again later.")
        }

        try {
            doExecute()
        } finally {
            isRunning.set(false)
        }
    }

    private fun doExecute() {
        val startTime = LocalDateTime.now()
        var isSuccess = true
        var creationTimeSec = 0.0
        var exception: Throwable? = null
        var result: Pair<Int, Int> = Pair(0, 0)

        runCatching {
            creationTimeSec =
                measureTimeMillis {
                    result = create()
                }.msToSeconds()
        }.onFailure { ex ->
            isSuccess = false
            log.error(ex) { "$regionName 콘텐츠 스케줄링 중 오류 발생" }
            exception = ex
        }.also {
            log.info {
                buildString {
                    appendLine("✅ isSuccess: $isSuccess")
                    appendLine("✅ 시작 시간: $startTime")
                    appendLine("✅ 소요 시간: $creationTimeSec")
                    appendLine("✅ message: ${exception?.cause?.message}")
                    append("✅ result: 생성(${result.first}) / 스킵(${result.second})")
                }
            }

            applicationEventPublisher.publishEvent(
                ContentsSchedulingEvent(
                    title = eventTitle,
                    isSuccess = isSuccess,
                    startTime = startTime,
                    totalTime = "%.3f".format(creationTimeSec),
                    message = if (isSuccess) "None" else exception?.cause?.message ?: "Unknown error",
                    result = if (isSuccess) "생성(${result.first}) / 스킵(${result.second})" else "None",
                ),
            )

            if (!isSuccess) {
                throw BadRequestException("$regionName 콘텐츠 스케줄링에 실패 : ${exception?.cause?.message}")
            }

            // Gen 스케줄링 완료 이벤트 발행 (성공 시에만)
            applicationEventPublisher.publishEvent(
                GenSchedulingCompletedEvent(
                    region = region,
                ),
            )
        }
    }

    private fun create(): Pair<Int, Int> {
        val urlsByCategories = scrapper.extractUrlsByCategories(region)

        var successCnt = 0
        var failCnt = 0
        val successCntByCategory = mutableMapOf<Category, Int>()
        val rawContentsToInsert = mutableListOf<RawContents>()

        val maxSize = urlsByCategories.values.maxOfOrNull { it.size } ?: 0

        /**
         * Step 1: 각 카테고리의 뉴스를 1개씩 순회하여 RawContents 객체 생성
         */
        for (i in 0 until maxSize) {
            urlsByCategories.forEach { (category, urls) ->
                if (successCntByCategory.getOrDefault(category, 0) >= contentsCountByCategory) {
                    return@forEach
                }

                urls.elementAtOrNull(i)?.let { url ->
                    try {
                        val rawContent = rawContentsService.createRawContent(url, category, region)

                        // Check for duplicate URL in rawContentsToInsert
                        if (rawContentsToInsert.any { it.url == rawContent.url }) {
                            throw BadRequestException("Duplicate URL detected: ${rawContent.url}")
                        }

                        rawContentsToInsert.add(rawContent)

                        successCntByCategory[category] = successCntByCategory.getOrDefault(category, 0) + 1
                        successCnt++
                    } catch (e: Exception) {
                        failCnt++
                        log.error(e) {
                            "$regionName RawContents 생성 중 오류 발생하여 Skip 처리. URL: $url, 카테고리: ${category.title}"
                        }
                    }
                }
            }
        }

        // Bulk insert all RawContents
        if (rawContentsToInsert.isEmpty()) {
            return successCnt to failCnt
        }
        val savedRawContents = rawContentsService.createAll(rawContentsToInsert)

        /**
         * Step 2: 저장된 RawContents를 기반으로 ProvisioningContents 객체 생성
         */
        val provisioningContentsToInsert = mutableListOf<ProvisioningContents>()
        val rawContentsMap = mutableMapOf<Long, RawContents>()

        savedRawContents.forEach { rawContent ->
            try {
                val provisioningContent = provisioningService.createProvisioningContent(rawContent)
                provisioningContentsToInsert.add(provisioningContent)
                rawContentsMap[rawContent.id!!] = rawContent
            } catch (e: Exception) {
                log.error(e) {
                    "$regionName ProvisioningContents 생성 중 오류 발생하여 Skip 처리. RawContents ID: ${rawContent.id}"
                }
            }
        }

        // Bulk insert all ProvisioningContents
        if (provisioningContentsToInsert.isEmpty()) {
            return successCnt to failCnt
        }
        val savedProvisioningContents = provisioningService.createAll(provisioningContentsToInsert)

        /**
         * Step 3: 저장된 ProvisioningContents를 기반으로 Gen 객체 생성
         */
        val gensToInsert = mutableListOf<Gen>()
        savedProvisioningContents.forEach { provisioningContent ->
            try {
                val rawContent = rawContentsMap[provisioningContent.rawContentsId]
                if (rawContent != null) {
                    val gen = genService.createGen(rawContent, provisioningContent)
                    gensToInsert.add(gen)
                }
            } catch (e: Exception) {
                log.error(e) {
                    "$regionName Gen 생성 중 오류 발생하여 Skip 처리. ProvisioningContents ID: ${provisioningContent.id}"
                }
            }
        }

        // Bulk insert all Gens
        if (gensToInsert.isNotEmpty()) {
            genService.createAll(gensToInsert)
        }

        return successCnt to failCnt
    }

    protected fun Long.msToSeconds(): Double = this / 1000.0
}
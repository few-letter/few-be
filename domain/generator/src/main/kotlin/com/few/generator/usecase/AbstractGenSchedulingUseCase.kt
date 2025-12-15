package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.common.exception.BadRequestException
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.event.dto.ContentsSchedulingEventDto
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
    protected fun executeInternal() {
        /** 0~15분 사이 랜덤으로 sleep 후 진행 **/
        Thread.sleep((0..15).random().toLong() * 60 * 1000)

        if (!isRunning.compareAndSet(false, true)) {
            throw BadRequestException("$schedulingName is already running. Please try again later.")
        }

        try {
            doExecute()
        } finally {
            isRunning.set(false)
        }
    }

    @GeneratorTransactional
    fun executeNow() {
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
                ContentsSchedulingEventDto(
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
        }
    }

    private fun create(): Pair<Int, Int> {
        val urlsByCategories = scrapper.extractUrlsByCategories(region)

        var successCnt = 0
        var failCnt = 0
        val successCntByCategory = mutableMapOf<Category, Int>()

        val maxSize = urlsByCategories.values.maxOfOrNull { it.size } ?: 0

        /**
         * 각 카테고리의 뉴스를 1개씩 순회하여 카테고리가 골고루 섞이도록 처리
         */
        for (i in 0 until maxSize) {
            urlsByCategories.forEach { (category, urls) ->
                if (successCntByCategory.getOrDefault(category, 0) >= contentsCountByCategory) {
                    return@forEach
                }

                urls.elementAtOrNull(i)?.let { url ->
                    try {
                        val rawContent = rawContentsService.create(url, category, region)
                        val provisioningContent = provisioningService.create(rawContent)
                        genService.create(rawContent, provisioningContent)

                        successCntByCategory[category] = successCntByCategory.getOrDefault(category, 0) + 1
                        successCnt++
                    } catch (e: Exception) {
                        failCnt++
                        log.error(e) {
                            "$regionName 콘텐츠 생성 중 오류 발생하여 Skip 처리. URL: $url, 카테고리: ${category.title}"
                        }
                    }
                }
            }
        }

        return successCnt to failCnt
    }

    protected fun Long.msToSeconds(): Double = this / 1000.0
}
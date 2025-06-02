package com.few.generator.usecase

import com.few.generator.domain.Category
import com.few.generator.domain.GenType
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.event.dto.ContentsSchedulingEventDto
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import web.handler.exception.BadRequestException
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

@Component
class SchedulingUseCase(
    private val rawContentsService: RawContentsService,
    private val provisioningService: ProvisioningService,
    private val genService: GenService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "\${scheduling.cron.generator}")
    @GeneratorTransactional
    fun execute() {
        val startTime = LocalDateTime.now()
        var rawContents = emptyMap<Category, List<RawContents>>()
        var provisionings = emptyMap<Category, List<ProvisioningContents>>()

        var timeOfCreatingRawContents = 0.0
        var timeOfCreatingProvisionings = 0.0
        var timeOfCreatingGens = 0.0
        var isSuccess = true

        runCatching {
            measureAndReturn { rawContentsService.create() }
                .also { (result, time) ->
                    rawContents = result
                    timeOfCreatingRawContents = time
                }

            measureAndReturn { createProvisionings(rawContents) }
                .also { (result, time) ->
                    provisionings = result
                    timeOfCreatingProvisionings = time
                }

            measureTimeMillis {
                createGens(rawContents, provisionings)
            }.msToSeconds().also { timeOfCreatingGens = it }
        }.onFailure {
            isSuccess = false
            log.error(it) { "콘텐츠 스케줄링 중 오류 발생" }
        }.also {
            val totalTime = timeOfCreatingRawContents + timeOfCreatingProvisionings + timeOfCreatingGens
            val countByCategory =
                rawContents.entries.joinToString(separator = "\n") { (category, rawList) ->
                    val count = rawList.count { it != null }
                    "[${category.title}] $count"
                }

            log.info {
                buildString {
                    appendLine("# isSuccess: $isSuccess")
                    appendLine("# 시작 시간: $startTime")
                    appendLine("# 카테고리 별 생성 개수: $countByCategory")
                    appendLine("✅ [1단계] RawContents: $timeOfCreatingRawContents s")
                    appendLine("✅ [2단계] Provisionings: $timeOfCreatingProvisionings s")
                    appendLine("✅ [3단계] Gens: $timeOfCreatingGens s")
                    append("-> 전체 : $totalTime s")
                }
            }

            applicationEventPublisher.publishEvent(
                ContentsSchedulingEventDto(
                    isSuccess = isSuccess,
                    startTime = startTime,
                    timeOfCreatingRawContents = "%.3f".format(timeOfCreatingRawContents),
                    timeOfCreatingProvisionings = "%.3f".format(timeOfCreatingProvisionings),
                    timeOfCreatingGens = "%.3f".format(timeOfCreatingGens),
                    totalTime = "%.3f".format(totalTime),
                    countByCategory = countByCategory,
                ),
            )

            if (!isSuccess) {
                throw BadRequestException("콘텐츠 스케줄링에 실패 : $startTime")
            }
        }
    }

    private fun createProvisionings(rawContents: Map<Category, List<RawContents>>): Map<Category, List<ProvisioningContents>> =
        rawContents.mapValues { (_, rawContents) ->
            rawContents.map { rawContent ->
                provisioningService.create(rawContent)
            }
        }

    private fun createGens(
        rawContents: Map<Category, List<RawContents>>,
        provisionings: Map<Category, List<ProvisioningContents>>,
    ) {
        val genTypes: Set<Int> = GenType.entries.map { it.code }.toSet()

        rawContents.forEach { (category, rawContentsList) ->
            val provisioningList = provisionings[category].orEmpty()

            rawContentsList.zip(provisioningList).forEach { (raw, provisioning) ->
                genService.create(raw, provisioning, genTypes)
            }
        }
    }

    private inline fun <T> measureAndReturn(block: () -> T): Pair<T, Double> {
        val start = System.currentTimeMillis()
        val result = block()
        val elapsed = System.currentTimeMillis() - start
        return result to elapsed.msToSeconds()
    }

    private fun Long.msToSeconds(): Double = this / 1000.0
}
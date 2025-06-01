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
        var rawContents = emptyMap<Category, List<RawContents>>()
        var provisionings = emptyMap<Category, List<ProvisioningContents>>()

        var timeOfCreatingRawContents = 0.0
        var timeOfCreatingProvisionings = 0.0
        var timeOfCreatingGens = 0.0

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
            log.error(it) { "콘텐츠 스케줄링 중 오류 발생" }
        }.also {
            val total = timeOfCreatingRawContents + timeOfCreatingProvisionings + timeOfCreatingGens

            log.info {
                buildString {
                    appendLine("✅ [1단계] RawContents: $timeOfCreatingRawContents s")
                    appendLine("✅ [2단계] Provisionings: $timeOfCreatingProvisionings s")
                    appendLine("✅ [3단계] Gens: $timeOfCreatingGens s")
                    append("-> 전체 : $total s")
                }
            }

            applicationEventPublisher.publishEvent(
                ContentsSchedulingEventDto(
                    timeOfCreatingRawContents = String.format("%.3f", timeOfCreatingRawContents),
                    timeOfCreatingProvisioning = String.format("%.3f", timeOfCreatingProvisionings),
                    timeOfCreatingGens = String.format("%.3f", timeOfCreatingGens),
                    total = String.format("%.3f", total),
                ),
            )
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

        rawContents.forEach { (category, rawList) ->
            val provisioningList = provisionings[category].orEmpty()

            rawList.zip(provisioningList).forEach { (raw, provisioning) ->
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
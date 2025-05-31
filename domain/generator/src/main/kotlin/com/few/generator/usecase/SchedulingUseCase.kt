package com.few.generator.usecase

import com.few.generator.domain.Category
import com.few.generator.domain.GenType
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Component
class SchedulingUseCase(
    private val rawContentsService: RawContentsService,
    private val provisioningService: ProvisioningService,
    private val genService: GenService,
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "\${scheduling.cron.generator}")
    @GeneratorTransactional
    fun execute() {
        // 1. 스크래핑 후 Raw 데이터 저장
        val (rawContents, timeOfCreatingRawContents) =
            measureAndReturn {
                rawContentsService.create()
            }

        // 2. raw 데이터 기반 provisioning 생성
        val (provisionings, timeOfCreatingProvisionings) =
            measureAndReturn {
                createProvisionings(rawContents)
            }

        // 3. gen 생성
        val timeOfCreatingGens =
            measureTimeMillis {
                createGens(rawContents, provisionings)
            }

        log.info {
            "✅ [1단계] RawContents: ${timeOfCreatingRawContents.msToSeconds()} s \n" +
                "✅ [2단계] Provisionings: ${timeOfCreatingProvisionings.msToSeconds()} s \n" +
                "✅ [3단계] Gens: ${timeOfCreatingGens.msToSeconds()} s \n" +
                "-> 전체 : ${(timeOfCreatingRawContents + timeOfCreatingProvisionings + timeOfCreatingGens).msToSeconds()} s"
        }

        // TODO: 스케줄링 결과 이력 저장
        // TODO: 수해 결과 디스코드 알림 추가
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

    private inline fun <T> measureAndReturn(block: () -> T): Pair<T, Long> {
        val start = System.currentTimeMillis()
        val result = block()
        val elapsed = System.currentTimeMillis() - start
        return result to elapsed
    }

    private fun Long.msToSeconds(): String = String.format("%.3f", this / 1000.0)
}
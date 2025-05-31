package com.few.generator.usecase

import com.few.generator.domain.Category
import com.few.generator.domain.GenType
import com.few.generator.domain.ProvisioningContents
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

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
        val rawContents = rawContentsService.create()

        // 2. raw 데이터 기반 provisioning 생성
        val provisionings: Map<Category, List<ProvisioningContents>> =
            rawContents.mapValues { (_, rawContents) ->
                rawContents.map { rawContent ->
                    provisioningService.create(rawContent)
                }
            }

        // 3. gen 생성
        val genTypes: Set<Int> = GenType.entries.map { it.code }.toSet()
        rawContents.forEach { (category, rawList) ->
            val provisioningList = provisionings[category].orEmpty()

            rawList.zip(provisioningList).forEach { (raw, provisioning) ->
                genService.create(raw, provisioning, genTypes)
            }
        }
    }
}
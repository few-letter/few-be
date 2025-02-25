package com.few.generator.usecase

import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ContentsGeneratorUseCase(
    private val rawContentsService: RawContentsService,
    private val provisioningService: ProvisioningService,
    private val genService: GenService,
) {
    private val log = KotlinLogging.logger {}

    @GeneratorTransactional
    fun execute(sourceUrl: String): List<String> {
        // 1. 스크래핑 후 Raw 데이터 저장
        val rawContents = rawContentsService.create(sourceUrl)

        // 2. raw 데이터 기반 provisioning 생성
        val provisioningContents = provisioningService.create(rawContents)

        // 3. gen 생성
        val gens = genService.create(rawContents, provisioningContents)

        return emptyList()
    }
}
package com.few.generator.usecase

import com.few.generator.domain.Category
import com.few.generator.domain.GenType
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.ContentsGeneratorUseCaseOut
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class CreateAllUseCase(
    private val rawContentsService: RawContentsService,
    private val provisioningService: ProvisioningService,
    private val genService: GenService,
) {
    private val log = KotlinLogging.logger {}

    @GeneratorTransactional
    fun execute(sourceUrl: String): ContentsGeneratorUseCaseOut {
        // 1. 스크래핑 후 Raw 데이터 저장
        val rawContents = rawContentsService.create(sourceUrl, Category.ETC) // TODO: remove

        // 2. raw 데이터 기반 provisioning 생성
        val provisioningContents = provisioningService.create(rawContents)

        // 3. gen 생성
        val gens = genService.create(rawContents, provisioningContents, GenType.values().map { it.code }.toSet())

        return ContentsGeneratorUseCaseOut(
            sourceUrl = sourceUrl,
            rawContentId = rawContents.id!!,
            provisioningContentId = provisioningContents.id!!,
            genIds = gens.map { it.id!! },
        )
    }
}
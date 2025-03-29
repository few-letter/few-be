package com.few.generator.usecase

import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.ContentsGeneratorUseCaseOut
import org.springframework.stereotype.Component

@Component
class CreateGenUseCase(
    private val rawContentsService: RawContentsService,
    private val provisioningService: ProvisioningService,
    private val genService: GenService,
) {
    @GeneratorTransactional
    fun execute(provContentsId: Long): ContentsGeneratorUseCaseOut { // TODO: 젠 타입에 따라 생성하도록 반영
        genService.validateExists(provContentsId)

        // 1. provisioning 조회
        val provisioningContents = provisioningService.getById(provContentsId)

        // 2. RawContents 조회
        val rawContents = rawContentsService.getById(provisioningContents.rawContentsId)

        // 3. gen 생성
        val gens = genService.create(rawContents, provisioningContents)

        return ContentsGeneratorUseCaseOut(
            sourceUrl = rawContents.url,
            rawContentId = rawContents.id!!,
            provisioningContentId = provisioningContents.id!!,
            genIds = gens.map { it.id!! },
        )
    }
}
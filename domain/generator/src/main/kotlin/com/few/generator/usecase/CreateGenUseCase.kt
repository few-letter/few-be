package com.few.generator.usecase

import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.ContentsGeneratorUseCaseOut
import org.springframework.stereotype.Component

@Component
@Deprecated(
    "Use CreateAllUseCase instead. This use case is deprecated and will be removed in the future.",
)
class CreateGenUseCase(
    private val rawContentsService: RawContentsService,
    private val provisioningService: ProvisioningService,
    private val genService: GenService,
) {
    @GeneratorTransactional
    fun execute(
        provContentsId: Long,
        requestedTypes: Set<Int>,
    ): ContentsGeneratorUseCaseOut {
        // 1. provisioning 조회
        val provisioningContents = provisioningService.getById(provContentsId)

        // 2. RawContents 조회
        val rawContents = rawContentsService.getById(provisioningContents.rawContentsId)

        // 3. 생성할 gen 타입 조회
        val existsGenTypes: Set<Int> =
            genService
                .getByProvisioningContentsId(provisioningContents.id!!)
                .map { it.typeCode }
                .toSet()

        val toBeCreatedTypes = requestedTypes - existsGenTypes

        // 4. gen 생성
        val gens = genService.create(rawContents, provisioningContents, toBeCreatedTypes)

        return ContentsGeneratorUseCaseOut(
            sourceUrl = rawContents.url,
            rawContentId = rawContents.id!!,
            provisioningContentId = provisioningContents.id!!,
            genIds = gens.map { it.id!! },
        )
    }
}
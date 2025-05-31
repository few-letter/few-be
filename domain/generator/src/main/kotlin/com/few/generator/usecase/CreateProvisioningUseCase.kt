package com.few.generator.usecase

import com.few.generator.domain.Category
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.ContentsGeneratorUseCaseOut
import org.springframework.stereotype.Component
import web.handler.exception.BadRequestException

@Component
@Deprecated(
    "Use CreateAllUseCase instead. This use case is deprecated and will be removed in the future.",
)
class CreateProvisioningUseCase(
    private val rawContentsService: RawContentsService,
    private val provisioningService: ProvisioningService,
) {
    @GeneratorTransactional
    fun execute(sourceUrl: String): ContentsGeneratorUseCaseOut {
        // 1. 스크래핑 후 Raw 데이터 저장
        val rawContents =
            rawContentsService.create(sourceUrl, Category.ETC)
                ?: throw BadRequestException("Failed to create raw contents for URL: $sourceUrl")

        // 2. raw 데이터 기반 provisioning 생성
        val provisioningContents = provisioningService.create(rawContents)

        return ContentsGeneratorUseCaseOut(
            sourceUrl = sourceUrl,
            rawContentId = rawContents.id!!,
            provisioningContentId = provisioningContents.id!!,
        )
    }
}
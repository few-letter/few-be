package com.few.generator.service

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.support.jpa.GeneratorTransactional
import org.springframework.stereotype.Service

@Service
class ContentsCommonGenerationService(
    protected val rawContentsService: RawContentsService,
    protected val provisioningService: ProvisioningService,
    protected val genService: GenService,
) {
    /**
     * RawContents, ProvisioningContents, Gen 중 1개라도 실패시 rollback하기 위해
     * 개별 트랜잭션으로 분리
     */
    @GeneratorTransactional
    open fun createSingleContents(
        url: String,
        category: Category,
        region: Region,
    ) {
        val rawContent = rawContentsService.createAndSave(url, category, region)
        val provisioningContent = provisioningService.createAndSave(rawContent)
        genService.createAndSave(rawContent, provisioningContent)
    }
}
package com.few.generator.usecase

import com.few.generator.domain.Gen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.repository.GenRepository
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.repository.RawContentsRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.BrowseContentsUsecaseOut
import com.few.generator.usecase.out.BrowseGenUsecaseOut
import com.few.generator.usecase.out.BrowseProvisioningContentsUsecaseOut
import com.few.generator.usecase.out.BrowseRawContentsUsecaseOut
import org.springframework.stereotype.Component

@Component
class RawContentsBrowseContentUseCase(
    private val rawContentsRepository: RawContentsRepository,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val genRepository: GenRepository,
) {
    @GeneratorTransactional(readOnly = true)
    fun execute(rawContentsId: Long): BrowseContentsUsecaseOut {
        val rawContents: RawContents =
            rawContentsRepository
                .findById(rawContentsId)
                .orElseThrow { RuntimeException("컨텐츠가 존재하지 않습니다.") }

        val provContents: ProvisioningContents =
            provisioningContentsRepository.findByRawContentsId(rawContents.id!!)
                ?: throw RuntimeException("프로비저닝 컨텐츠가 존재하지 않습니다.")

        val gens: List<Gen> = genRepository.findByProvisioningContentsId(provContents.id!!)

        return BrowseContentsUsecaseOut(
            rawContents =
                BrowseRawContentsUsecaseOut(
                    id = rawContents.id!!,
                    url = rawContents.url,
                    title = rawContents.title,
                    description = rawContents.description,
                    thumbnailImageUrl = rawContents.thumbnailImageUrl,
                    rawTexts = rawContents.rawTexts,
                    imageUrls = rawContents.imageUrls,
                    createdAt = rawContents.createdAt!!,
                ),
            provisioningContents =
                BrowseProvisioningContentsUsecaseOut(
                    id = provContents.id!!,
                    rawContentsId = provContents.rawContentsId,
                    completionIds = provContents.completionIds,
                    bodyTextsJson = provContents.bodyTextsJson,
                    coreTextsJson = provContents.coreTextsJson,
                    createdAt = provContents.createdAt!!,
                ),
            gens =
                gens.map {
                    BrowseGenUsecaseOut(
                        id = it.id!!,
                        provisioningContentsId = it.provisioningContentsId,
                        completionIds = it.completionIds,
                        headline = it.headline,
                        summary = it.summary,
                        highlightTexts = it.highlightTexts,
                        createdAt = it.createdAt!!,
                    )
                },
        )
    }
}
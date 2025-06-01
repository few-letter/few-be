package com.few.generator.usecase

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.domain.*
import com.few.generator.repository.GenRepository
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.repository.RawContentsRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class RawContentsBrowseContentUseCase(
    private val rawContentsRepository: RawContentsRepository,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val genRepository: GenRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
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
                    imageUrls = gson.fromJson(rawContents.imageUrls, object : TypeToken<List<String>>() {}.type),
                    createdAt = rawContents.createdAt!!,
                ),
            provisioningContents =
                BrowseProvisioningContentsUsecaseOut(
                    id = provContents.id!!,
                    rawContentsId = provContents.rawContentsId,
                    completionIds = provContents.completionIds,
                    bodyTextsJson =
                        gson.fromJson(
                            provContents.bodyTextsJson,
                            object : TypeToken<List<String>>() {}.type,
                        ),
                    coreTextsJson =
                        gson.fromJson(
                            provContents.coreTextsJson,
                            object : TypeToken<List<String>>() {}.type,
                        ),
                    category =
                        CodeValue(
                            code = Category.from(provContents.category).code,
                            value = Category.from(provContents.category).name,
                        ),
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
                        highlightTexts = gson.fromJson(it.highlightTexts, object : TypeToken<List<String>>() {}.type),
                        type =
                            CodeValue(
                                code = GenType.from(it.typeCode).code,
                                value = GenType.from(it.typeCode).title,
                            ),
                        createdAt = it.createdAt!!,
                    )
                },
        )
    }
}
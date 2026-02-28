package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.domain.Gen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.repository.GenRepository
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class RawContentsBrowseContentUseCase(
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val genRepository: GenRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    @GeneratorTransactional(readOnly = true)
    fun execute(genId: Long): BrowseContentsUsecaseOut {
        val gen: Gen =
            genRepository
                .findById(genId)
                .orElseThrow { RuntimeException("Gen 컨텐츠가 존재하지 않습니다.") }

        val provContents: ProvisioningContents =
            provisioningContentsRepository
                .findById(gen.provisioningContentsId)
                .orElseThrow { RuntimeException("프로비저닝 컨텐츠가 존재하지 않습니다.") }

        return BrowseContentsUsecaseOut(
            rawContents =
                BrowseRawContentsUsecaseOut(
                    url = gen.url,
                    thumbnailImageUrl = gen.thumbnailImageUrl,
                    mediaType = MediaType.from(gen.mediaType),
                    createdAt = gen.createdAt!!,
                ),
            provisioningContents =
                BrowseProvisioningContentsUsecaseOut(
                    id = provContents.id!!,
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
                    createdAt = provContents.createdAt!!,
                ),
            gen =
                BrowseGenUsecaseOut(
                    id = gen.id!!,
                    provisioningContentsId = gen.provisioningContentsId,
                    completionIds = gen.completionIds,
                    headline = gen.headline,
                    summary = gen.summary,
                    highlightTexts = gson.fromJson(gen.highlightTexts, object : TypeToken<List<String>>() {}.type),
                    category = Category.from(gen.category),
                    region = gen.region?.let { Region.from(it) },
                    createdAt = gen.createdAt!!,
                ),
        )
    }
}
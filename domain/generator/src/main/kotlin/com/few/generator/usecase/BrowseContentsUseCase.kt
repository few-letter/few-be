package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.repository.GenRepository
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.repository.RawContentsRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.input.BrowseContentsUseCaseIn
import com.few.generator.usecase.out.BrowseContentsUsecaseOuts
import com.few.generator.usecase.out.ContentsUsecaseOut
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.Int
import kotlin.String

@Component
data class BrowseContentsUseCase(
    private val rawContentsRepository: RawContentsRepository,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val genRepository: GenRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
    @Value("\${generator.contents.pageSize}")
    private val pageSize: Int,
) {
    @GeneratorTransactional(readOnly = true)
    fun execute(input: BrowseContentsUseCaseIn): BrowseContentsUsecaseOuts {
        val gens =
            when {
                input.categoryCode != null -> {
                    if (input.prevGenId == -1L) {
                        genRepository.findFirstLimitByCategory(input.categoryCode, pageSize)
                    } else {
                        genRepository.findNextLimitByCategory(input.prevGenId, input.categoryCode, pageSize)
                    }
                }
                else -> {
                    if (input.prevGenId == -1L) {
                        genRepository.findFirstLimit(pageSize)
                    } else {
                        genRepository.findNextLimit(input.prevGenId, pageSize)
                    }
                }
            }

        if (gens.isEmpty()) {
            return BrowseContentsUsecaseOuts(
                contents = emptyList(),
                isLast = true,
            )
        }

        val joinedContents =
            gens.mapNotNull { gen ->
                val provisioning = provisioningContentsRepository.findById(gen.provisioningContentsId).orElse(null)
                val rawContentsId = provisioning?.rawContentsId ?: return@mapNotNull null
                val rawContents = rawContentsRepository.findById(rawContentsId).orElse(null)
                if (rawContents != null) Triple(gen, provisioning, rawContents) else null
            }

        return BrowseContentsUsecaseOuts(
            contents =
                joinedContents.map { (gen, _, raw) ->
                    ContentsUsecaseOut(
                        id = gen.id!!,
                        url = raw.url,
                        thumbnailImageUrl = raw.thumbnailImageUrl,
                        mediaType = MediaType.from(raw.mediaType),
                        headline = gen.headline,
                        summary = gen.summary,
                        highlightTexts = gson.fromJson(gen.highlightTexts, object : TypeToken<List<String>>() {}.type),
                        createdAt = gen.createdAt ?: LocalDateTime.MIN,
                        category = Category.from(gen.category),
                    )
                },
            isLast = gens.size < pageSize,
        )
    }
}
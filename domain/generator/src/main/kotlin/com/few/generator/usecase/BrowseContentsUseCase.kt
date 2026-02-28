package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.repository.GenRepository
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

@Component
data class BrowseContentsUseCase(
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
                input.category != null -> {
                    if (input.prevGenId == -1L) {
                        genRepository.findFirstLimitByCategory(input.category.code, pageSize, input.region.code)
                    } else {
                        genRepository.findNextLimitByCategory(input.prevGenId, input.category.code, pageSize, input.region.code)
                    }
                }
                else -> {
                    if (input.prevGenId == -1L) {
                        genRepository.findFirstLimit(pageSize, input.region.code)
                    } else {
                        genRepository.findNextLimit(input.prevGenId, pageSize, input.region.code)
                    }
                }
            }

        if (gens.isEmpty()) {
            return BrowseContentsUsecaseOuts(
                contents = emptyList(),
                isLast = true,
            )
        }

        return BrowseContentsUsecaseOuts(
            contents =
                gens.map { gen ->
                    ContentsUsecaseOut(
                        id = gen.id!!,
                        url = gen.url,
                        thumbnailImageUrl = gen.thumbnailImageUrl,
                        mediaType = MediaType.from(gen.mediaType),
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
package com.few.generator.usecase

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.repository.GenRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.BrowseContentsUsecaseOut
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class RawContentsBrowseContentUseCase(
    private val genRepository: GenRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    @GeneratorTransactional(readOnly = true)
    fun execute(genId: Long): BrowseContentsUsecaseOut {
        val gen =
            genRepository
                .findById(genId)
                .orElseThrow { RuntimeException("Gen 컨텐츠가 존재하지 않습니다.") }

        return BrowseContentsUsecaseOut(
            id = gen.id!!,
            url = gen.url.orEmpty(),
            thumbnailImageUrl = gen.thumbnailImageUrl,
            mediaType = gen.mediaType,
            headline = gen.headline,
            summary = gen.summary,
            highlightTexts = gson.fromJson(gen.highlightTexts, object : TypeToken<List<String>>() {}.type),
            category = gen.category,
            region = gen.region,
            createdAt = gen.createdAt!!,
        )
    }
}
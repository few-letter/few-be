package com.few.generator.service

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.domain.RawContents
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class RawContentsService(
    private val scrapper: Scrapper,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    fun create(
        url: String,
        category: Category,
        region: Region,
    ): RawContents {
        val scrappedResult = scrapper.scrape(url)

        return RawContents(
            url = scrappedResult.sourceUrl,
            title = scrappedResult.title,
            thumbnailImageUrl = scrappedResult.thumbnailImageUrl,
            rawTexts = scrappedResult.rawTexts.joinToString("\n"),
            imageUrls = gson.toJson(scrappedResult.images),
            category = category.code,
            mediaType = MediaType.find(scrappedResult.sourceUrl).code,
            region = region.code,
        )
    }
}
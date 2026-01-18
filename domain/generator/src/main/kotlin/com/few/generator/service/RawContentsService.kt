package com.few.generator.service

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.common.exception.BadRequestException
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.domain.RawContents
import com.few.generator.repository.RawContentsRepository
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class RawContentsService(
    private val scrapper: Scrapper,
    private val rawContentsRepository: RawContentsRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun createAndSave(
        url: String,
        category: Category,
        region: Region,
    ): RawContents {
        val scrappedResult = scrapper.scrape(url)

        rawContentsRepository.findByUrl(scrappedResult.sourceUrl)?.let {
            throw BadRequestException("이미 생성된 컨텐츠가 있습니다. ID: ${it.id}, URL: ${scrappedResult.sourceUrl}")
        }

        return rawContentsRepository.save(
            RawContents(
                url = scrappedResult.sourceUrl,
                title = scrappedResult.title,
                thumbnailImageUrl = scrappedResult.thumbnailImageUrl,
                rawTexts = scrappedResult.rawTexts.joinToString("\n"),
                imageUrls = gson.toJson(scrappedResult.images),
                category = category.code,
                mediaType = MediaType.find(scrappedResult.sourceUrl).code,
                region = region.code,
            ),
        )
    }

    fun findAllByIdIn(ids: List<Long>): List<RawContents> = rawContentsRepository.findAllByIdIn(ids)
}
package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.Scrapper
import com.few.generator.domain.RawContents
import com.few.generator.repository.RawContentsRepository
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import web.handler.exception.BadRequestException

@Service
class RawContentsService(
    private val scrapper: Scrapper,
    private val rawContentsRepository: RawContentsRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun create(sourceUrl: String): RawContents {
        rawContentsRepository.findByUrl(sourceUrl)?.let {
            throw BadRequestException("이미 생성된 컨텐츠가 있습니다. ID: ${it.id}")
        }

        val scrappedResult = scrapper.scrape(sourceUrl) ?: throw BadRequestException("스크래핑 실패")

        val rawContents =
            RawContents(
                url = sourceUrl,
                title = scrappedResult.title,
                description = scrappedResult.description,
                thumbnailImageUrl = scrappedResult.thumbnailImageUrl,
                rawTexts = scrappedResult.rawTexts.joinToString("\n"),
                imageUrls = gson.toJson(scrappedResult.images) ?: "[]",
            )

        return rawContentsRepository.save(rawContents)
    }

    fun getById(id: Long): RawContents =
        rawContentsRepository
            .findById(id)
            .orElseThrow { BadRequestException("Raw 컨텐츠가 존재하지 않습니다.") }
}
package com.few.generator.service

import com.few.generator.domain.Category
import com.few.generator.domain.Gen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.repository.GenRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class GenService(
    private val contentGenerationService: ContentGenerationService,
    private val genRepository: GenRepository,
) {
    private val log = KotlinLogging.logger {}

    fun create(
        rawContent: RawContents,
        provisioningContent: ProvisioningContents,
    ): Gen {
        log.info { "Gen 생성 시작: provisioningContentId=${provisioningContent.id}" }

        val generatedContent = contentGenerationService.generateContent(rawContent, provisioningContent)

        val gen = createGenEntity(provisioningContent, generatedContent)
        val savedGen = genRepository.save(gen)

        log.info { "Gen 생성 완료: id=${savedGen.id}, headline=${savedGen.headline}" }
        return savedGen
    }

    private fun createGenEntity(
        provisioningContent: ProvisioningContents,
        generatedContent: GeneratedContent,
    ): Gen =
        Gen(
            provisioningContentsId = provisioningContent.id!!,
            completionIds = generatedContent.completionIds.toMutableList(),
            headline = generatedContent.headline.headline,
            summary = generatedContent.summary.summary,
            highlightTexts = generatedContent.highlightTexts,
            category = Category.from(provisioningContent.category).code,
        )
}
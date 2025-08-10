package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightText
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.repository.GenRepository
import com.google.gson.Gson
import common.domain.Category
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class GenService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    private val genRepository: GenRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun create(
        rawContent: RawContents,
        provisioningContent: ProvisioningContents,
    ): Gen {
        val headlinePrompt =
            promptGenerator.toHeadlineShort(
                title = rawContent.title,
                description = rawContent.description,
                coreTextsJson = provisioningContent.coreTextsJson,
            )
        val headline: Headline = chatGpt.ask(headlinePrompt) as Headline

        val summaryPrompt =
            promptGenerator.toSummaryShort(
                headline = headline.headline,
                title = rawContent.title,
                description = rawContent.description,
                coreTextsJson = provisioningContent.coreTextsJson!!,
            )
        val summary: Summary = chatGpt.ask(summaryPrompt) as Summary

        val highlightTextPrompt = promptGenerator.toKoreanHighlightText(summary.summary)
        val highlightText: HighlightText = chatGpt.ask(highlightTextPrompt) as HighlightText

        return genRepository.save(
            Gen(
                provisioningContentsId = provisioningContent.id!!,
                completionIds = mutableListOf(headline.completionId!!, summary.completionId!!, highlightText.completionId!!),
                headline = headline.headline,
                summary = summary.summary,
                highlightTexts = gson.toJson(listOf(highlightText.highlightText)),
                category = Category.from(provisioningContent.category).code,
            ),
        )
    }
}
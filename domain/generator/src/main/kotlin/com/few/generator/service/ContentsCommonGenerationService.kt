package com.few.generator.service

import com.few.common.domain.Category
import com.few.common.domain.ContentsType
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ContentsCommonGenerationService(
    protected val rawContentsService: RawContentsService,
    protected val provisioningService: ProvisioningService,
    protected val genService: GenService,
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    open fun createSingleContents(
        url: String,
        category: Category,
        region: Region,
    ) {
        val rawContent = rawContentsService.create(url, category, region)
        val provisioningContent = provisioningService.create(rawContent)

        val headlinePrompt =
            promptGenerator.toHeadlineShort(
                title = rawContent.title,
                coreTextsJson = provisioningContent.coreTextsJson,
            )
        val headline: Headline = chatGpt.ask(headlinePrompt) as Headline

        val summaryPrompt =
            promptGenerator.toSummaryShort(
                headline = headline.headline,
                title = rawContent.title,
                coreTextsJson = provisioningContent.coreTextsJson,
            )
        val summary: Summary = chatGpt.ask(summaryPrompt) as Summary

        val highlightTextPrompt = promptGenerator.toKoreanHighlightText(summary.summary)
        val highlightTexts: HighlightTexts = chatGpt.ask(highlightTextPrompt) as HighlightTexts

        genService.saveWithNewTx(
            Gen(
                url = rawContent.url,
                thumbnailImageUrl = rawContent.thumbnailImageUrl,
                mediaType = MediaType.from(rawContent.mediaType),
                headline = headline.headline,
                summary = summary.summary,
                highlightTexts = gson.toJson(highlightTexts.highlightTexts),
                coreTextsJson = provisioningContent.coreTextsJson,
                category = Category.from(provisioningContent.category),
                region = Region.from(provisioningContent.region),
                contentsType =
                    if (Region.from(provisioningContent.region) == Region.GLOBAL) {
                        ContentsType.GLOBAL_NEWS
                    } else {
                        ContentsType.LOCAL_NEWS
                    },
            ),
        )
    }
}
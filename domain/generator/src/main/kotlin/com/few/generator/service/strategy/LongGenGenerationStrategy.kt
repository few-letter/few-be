package com.few.generator.service.strategy

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.domain.GenType
import com.few.generator.support.common.Constant
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component(Constant.GEN.STRATEGY_NAME_LONG)
class LongGenGenerationStrategy(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) : GenGenerationStrategy {
    override fun generate(material: Material): Gen {
        val headlinePrompt =
            promptGenerator.toHeadlineLong(
                title = material.title!!,
                description = material.description!!,
                coreTextsJson = material.coreTextsJson!!,
            )
        val headline: Headline = chatGpt.ask(headlinePrompt) as Headline

        val summaryPrompt =
            promptGenerator.toSummaryLong(
                title = material.title!!,
                description = material.description!!,
                summary = material.coreTextsJson!!,
            )
        val summary: Summary = chatGpt.ask(summaryPrompt) as Summary

        val highlightTextPrompt = promptGenerator.toHighlightTextsLong(summary.summary)
        val highlightTexts: HighlightTexts = chatGpt.ask(highlightTextPrompt) as HighlightTexts

        return Gen(
            provisioningContentsId = material.provisioningContentsId,
            completionIds = mutableListOf(headline.completionId!!, summary.completionId!!, highlightTexts.completionId!!),
            headline = headline.headline,
            summary = summary.summary,
            highlightTexts = gson.toJson(highlightTexts.highlightTexts),
            typeCode = GenType.STRATEGY_NAME_LONG.code,
        )
    }
}
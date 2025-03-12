package com.few.generator.service.strategy

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.service.strategy.GenGenerationStrategy.Companion.STRATEGY_NAME_KOREAN_LONG_QUESTION
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component(STRATEGY_NAME_KOREAN_LONG_QUESTION)
class KoreanLongQuestionGenGenerationStrategy(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) : GenGenerationStrategy {
    override fun generate(material: Material): Gen {
        val headlinePrompt =
            promptGenerator.toHeadlineKoreanLongQuestion(
                material.title!!,
                material.description!!,
                material.headline!!,
                material.summary!!,
            )
        val headline: Headline = chatGpt.ask(headlinePrompt) as Headline

        val summaryPrompt =
            promptGenerator.toSummaryKoreanLongQuestion(
                material.title!!,
                material.description!!,
                material.coreTextsJson!!,
                headline.headline,
                material.summary!!,
            )
        val summary: Summary = chatGpt.ask(summaryPrompt) as Summary

        val highlightTextPrompt = promptGenerator.toKoreanHighlightTexts(summary.summary)
        val highlightTexts: HighlightTexts = chatGpt.ask(highlightTextPrompt) as HighlightTexts

        return Gen(
            provisioningContentsId = material.provisioningContentsId,
            completionIds = mutableListOf(headline.completionId!!, summary.completionId!!),
            headline = headline.headline,
            summary = summary.summary,
            highlightTexts = gson.toJson(highlightTexts.highlightTexts),
        )
    }
}
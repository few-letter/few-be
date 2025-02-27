package com.few.generator.service.strategy

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightText
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.service.strategy.GenGenerationStrategy.Companion.STRATEGY_NAME_KOREAN_QUESTION
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component(STRATEGY_NAME_KOREAN_QUESTION)
class KoreanQuestionGenGenerationStrategy(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) : GenGenerationStrategy {
    override fun generate(material: Material): Gen {
        val headlinePrompt =
            promptGenerator.toHeadlineKoreanQuestion(
                material.title!!,
                material.description!!,
                material.headline!!,
                material.summary!!,
            )
        val headline: Headline = chatGpt.ask(headlinePrompt) as Headline

        val summaryPrompt =
            promptGenerator.toSummaryKoreanQuestion(
                headline.headline,
                material.title!!,
                material.description!!,
                material.coreTextsJson!!,
            )
        val summary: Summary = chatGpt.ask(summaryPrompt) as Summary

        val highlightTextPrompt = promptGenerator.toKoreanHighlightText(summary.summary)
        val highlight: HighlightText = chatGpt.ask(highlightTextPrompt) as HighlightText

        return Gen(
            headline = headline.headline,
            summary = summary.summary,
            highlightTexts = gson.toJson(listOf(highlight.highlightText)),
        )
    }
}
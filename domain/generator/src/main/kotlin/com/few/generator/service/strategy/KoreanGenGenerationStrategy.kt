package com.few.generator.service.strategy

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.service.strategy.GenGenerationStrategy.Companion.STRATEGY_NAME_KOREAN
import org.springframework.stereotype.Component

@Component(STRATEGY_NAME_KOREAN)
class KoreanGenGenerationStrategy(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
) : GenGenerationStrategy {
    override fun generate(material: Material): Gen {
        val headlinePrompt =
            promptGenerator.toHeadlineKorean(
                material.title!!,
                material.description!!,
                material.headline!!,
                material.summary!!,
            )
        val headline: Headline = chatGpt.ask(headlinePrompt) as Headline

        val summaryPrompt =
            promptGenerator.toSummaryKorean(
                material.title!!,
                material.description!!,
                material.coreTextsJson!!,
                material.headline!!,
                material.summary!!,
            )
        val summary: Summary = chatGpt.ask(summaryPrompt) as Summary

        return Gen(
            headline = headline.headline,
            summary = summary.summary,
        )
    }
}
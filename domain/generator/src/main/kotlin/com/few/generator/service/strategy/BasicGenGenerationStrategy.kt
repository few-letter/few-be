package com.few.generator.service.strategy

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.service.strategy.GenGenerationStrategy.Companion.STRATEGY_NAME_BASIC
import org.springframework.stereotype.Component

@Component(STRATEGY_NAME_BASIC)
class BasicGenGenerationStrategy(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
) : GenGenerationStrategy {
    override fun generate(material: Material): Gen {
        val headlinePrompt = promptGenerator.toHeadlineDefault(material.title!!, material.description!!, material.coreTextsJson!!)
        val headline: Headline = chatGpt.ask(headlinePrompt) as Headline

        val summaryPrompt = promptGenerator.toSummaryDefault(material.title!!, material.description!!, material.coreTextsJson!!)
        val summary: Summary = chatGpt.ask(summaryPrompt) as Summary

        return Gen(
            provisioningContentsId = material.provisioningContentsId,
            completionIds = mutableListOf(headline.completionId!!, summary.completionId!!),
            headline = headline.headline,
            summary = summary.summary,
        )
    }
}
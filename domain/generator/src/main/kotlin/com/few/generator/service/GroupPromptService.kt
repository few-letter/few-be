package com.few.generator.service

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Group
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import org.springframework.stereotype.Component

@Component
class GroupPromptService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
) {
    fun groupWebGen(
        webGenDetails: List<Pair<String, String>>,
        targetPercentage: Int = 30,
    ): Group {
        val prompt = promptGenerator.toCombinedGroupingPrompt(webGenDetails, targetPercentage)
        return chatGpt.ask(prompt) as Group
    }

    fun groupHeadline(headlines: List<String>): Headline {
        val prompt = promptGenerator.toGroupHeadlineOnlyPrompt(headlines)
        return chatGpt.ask(prompt) as Headline
    }

    fun groupSummary(
        groupHeadline: String,
        headlines: List<String>,
        summaries: List<String>,
    ): Summary {
        val prompt = promptGenerator.toGroupSummaryWithHeadlinesPrompt(groupHeadline, headlines, summaries)
        return chatGpt.ask(prompt) as Summary
    }

    fun groupHighlights(groupSummary: String): HighlightTexts {
        val prompt = promptGenerator.toGroupHighlightPrompt(groupSummary)
        return chatGpt.ask(prompt) as HighlightTexts
    }
}
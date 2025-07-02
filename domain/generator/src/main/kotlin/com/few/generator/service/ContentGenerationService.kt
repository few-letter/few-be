package com.few.generator.service

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

data class GeneratedContent(
    val headline: Headline,
    val summary: Summary,
    val highlightTexts: String,
    val completionIds: List<String>,
)

@Service
class ContentGenerationService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    private val highlightTextService: HighlightTextService,
) {
    private val log = KotlinLogging.logger {}

    fun generateContent(
        rawContent: RawContents,
        provisioningContent: ProvisioningContents,
    ): GeneratedContent {
        log.info { "콘텐츠 생성 시작: ${rawContent.title}" }

        val headline = generateHeadline(rawContent, provisioningContent)
        val summary = generateSummary(headline, rawContent, provisioningContent)
        val highlightTexts = highlightTextService.generateHighlightText(summary.summary)
        val highlightCompletionId = highlightTextService.extractCompletionId(summary.summary)

        val completionIds = buildCompletionIdsList(headline, summary, highlightCompletionId)

        log.info { "콘텐츠 생성 완료: ${headline.headline}" }

        return GeneratedContent(
            headline = headline,
            summary = summary,
            highlightTexts = highlightTexts,
            completionIds = completionIds,
        )
    }

    private fun generateHeadline(
        rawContent: RawContents,
        provisioningContent: ProvisioningContents,
    ): Headline {
        log.debug { "헤드라인 생성 시작" }

        val headlinePrompt =
            promptGenerator.toHeadlineShort(
                title = rawContent.title,
                description = rawContent.description,
                coreTextsJson = provisioningContent.coreTextsJson,
            )

        val headline = chatGpt.ask(headlinePrompt) as Headline
        log.debug { "헤드라인 생성 완료: ${headline.headline}" }

        return headline
    }

    private fun generateSummary(
        headline: Headline,
        rawContent: RawContents,
        provisioningContent: ProvisioningContents,
    ): Summary {
        log.debug { "요약 생성 시작" }

        val summaryPrompt =
            promptGenerator.toSummaryShort(
                headline = headline.headline,
                title = rawContent.title,
                description = rawContent.description,
                coreTextsJson = provisioningContent.coreTextsJson!!,
            )

        val summary = chatGpt.ask(summaryPrompt) as Summary
        log.debug { "요약 생성 완료" }

        return summary
    }

    private fun buildCompletionIdsList(
        headline: Headline,
        summary: Summary,
        highlightCompletionId: String?,
    ): List<String> {
        val completionIds = mutableListOf<String>()

        headline.completionId?.let { completionIds.add(it) }
        summary.completionId?.let { completionIds.add(it) }
        highlightCompletionId?.let { completionIds.add(it) }

        return completionIds
    }
}
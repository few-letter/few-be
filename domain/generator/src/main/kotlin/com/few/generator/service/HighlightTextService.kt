package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.HighlightText
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class HighlightTextService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun generateHighlightText(summary: String): String {
        log.debug { "하이라이트 텍스트 생성 시작" }

        val highlightTextPrompt = promptGenerator.toKoreanHighlightText(summary)
        val highlightText: HighlightText = chatGpt.ask(highlightTextPrompt) as HighlightText

        val result = gson.toJson(listOf(highlightText.highlightText))

        log.debug { "하이라이트 텍스트 생성 완료: ${highlightText.highlightText}" }
        return result
    }

    fun extractCompletionId(summary: String): String? =
        try {
            val highlightTextPrompt = promptGenerator.toKoreanHighlightText(summary)
            val highlightText: HighlightText = chatGpt.ask(highlightTextPrompt) as HighlightText
            highlightText.completionId
        } catch (e: Exception) {
            log.warn(e) { "하이라이트 텍스트 completion ID 추출 실패" }
            null
        }
}
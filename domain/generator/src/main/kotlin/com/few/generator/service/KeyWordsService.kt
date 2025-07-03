package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Keywords
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class KeyWordsService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun generateKeyWords(coreTexts: String): String {
        log.debug { "키워드 생성 시작" }

        val keyWordsPrompt = promptGenerator.toKoreanKeyWords(coreTexts)
        val keywords: Keywords = chatGpt.ask(keyWordsPrompt) as Keywords
        val result = gson.toJson(keywords.keywords)

        log.debug { "키워드 생성 완료: ${keywords.keywords}" }
        return result
    }
}
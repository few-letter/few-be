package com.few.generator.service

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Keywords
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class KeyWordsService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
) {
    private val log = KotlinLogging.logger {}

    fun generateKeyWords(coreTexts: String): String {
        log.debug { "키워드 생성 시작: coreTexts 길이=${coreTexts.length}" }

        val prompt = promptGenerator.toKoreanKeyWords(coreTexts)
        val keywords: Keywords = chatGpt.ask(prompt) as Keywords

        log.debug { "키워드 생성 완료: ${keywords.keywords.size}개" }
        return keywords.keywords.joinToString(", ")
    }

    @Async("keywordExtractorExecutor")
    fun generateKeyWordsAsync(coreTexts: String): CompletableFuture<String> {
        log.debug { "비동기 키워드 생성 시작: coreTexts 길이=${coreTexts.length}" }

        return try {
            val prompt = promptGenerator.toKoreanKeyWords(coreTexts)
            val keywords: Keywords = chatGpt.ask(prompt) as Keywords
            val result = keywords.keywords.joinToString(", ")

            log.debug { "비동기 키워드 생성 완료: ${keywords.keywords.size}개" }
            CompletableFuture.completedFuture(result)
        } catch (e: Exception) {
            log.error(e) { "비동기 키워드 생성 실패" }
            CompletableFuture.completedFuture("키워드 추출 실패")
        }
    }

    suspend fun generateKeyWordsWithCoroutine(coreTexts: String): String {
        log.debug { "코루틴 키워드 생성 시작: coreTexts 길이=${coreTexts.length}" }

        return withContext(Dispatchers.IO) {
            try {
                val prompt = promptGenerator.toKoreanKeyWords(coreTexts)
                val keywords: Keywords = chatGpt.ask(prompt) as Keywords
                val result = keywords.keywords.joinToString(", ")

                log.debug { "코루틴 키워드 생성 완료: ${keywords.keywords.size}개" }
                result
            } catch (e: Exception) {
                log.error(e) { "코루틴 키워드 생성 실패" }
                "키워드 추출 실패"
            }
        }
    }
}
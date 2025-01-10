package com.few.generator.core

import com.few.generator.client.GeneratorOpenAiClient
import com.few.generator.client.request.OpenAiRequest
import com.few.generator.core.model.ContentSpec
import com.few.generator.core.model.GroupContentSpec
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ChatGpt(
    private val openAiClient: GeneratorOpenAiClient,
    private val mapper: Gson,
    private val promptGenerator: PromptGenerator,
    @Value("\${openai.api.model.basic}") private val AI_BASIC_MODEL: String,
    @Value("\${openai.api.model.advanced}") private val AI_ADVANCED_MODEL: String,
) {
    fun summarize(contentSpec: ContentSpec): JsonObject = doAsk(promptGenerator.createSummaryPrompt(contentSpec), AI_BASIC_MODEL)

    fun group(contentSpecList: List<ContentSpec>): JsonObject =
        doAsk(promptGenerator.createGroupingPrompt(contentSpecList), AI_ADVANCED_MODEL)

    fun summarizeGroup(group: GroupContentSpec): JsonObject = doAsk(promptGenerator.createSummaryPrompt(group), AI_BASIC_MODEL)

    fun refineSummarizedGroup(group: GroupContentSpec): JsonObject = doAsk(promptGenerator.createRefinePrompt(group), AI_BASIC_MODEL)

    /**
     * 공통된 OpenAI 요청 처리 및 JSON 결과 반환
     */
    private fun doAsk(
        prompt: List<Map<String, String>>,
        aiModel: String,
    ): JsonObject {
        val request =
            OpenAiRequest(
                model = aiModel,
                messages = prompt,
            )

        val response = openAiClient.send(request)
        val resultContent =
            response.choices
                .firstOrNull()
                ?.message
                ?.content
                ?.trim()
                ?: throw Exception("요약 결과를 찾을 수 없습니다.")

        return mapper.fromJson(resultContent, JsonObject::class.java)
    }
}
package com.few.generator.core.gpt.completion

import com.few.generator.core.gpt.prompt.schema.Schema
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

data class ChatCompletion(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null,
    val system_fingerprint: String? = null,
) {
    companion object { // TODO: gson 빈 등록된거 사용하도록 변경 또는 ???
        val gson =
            GsonBuilder()
                .setLenient()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()
    }

    // TODO: chatGpt.ask 메소드에 AOP 적용하여 ChatCompletion 메소드 호출 없이 변환되어 필드로 기저장되도록 변경
    // Or FeginClient Interceptor에 적용 (prompt.response_format.classType 필드 공유되어야 함)
    fun <T : Schema> getFirstChoiceMessage(type: Type): T =
        gson.fromJson(
            choices
                ?.find { it.index == 0 }
                ?.message
                ?.content ?: throw RuntimeException("No response found in $id completion"),
            type,
        )
}

data class Choice(
    val index: Int? = null,
    val message: Message? = null,
    val logprobs: Boolean? = null,
    val finish_reason: String? = null,
)

data class Message(
    val role: String? = null,
    val content: String? = null,
    val refusal: String? = null,
)

data class Usage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
    val prompt_tokens_details: PromptTokensDetails? = null,
    val completion_tokens_details: CompletionTokensDetails? = null,
)

data class PromptTokensDetails(
    val cached_tokens: Int? = null,
)

data class CompletionTokensDetails(
    val reasoning_tokens: Int? = null,
    val accepted_prediction_tokens: Int? = null,
    val rejected_prediction_tokens: Int? = null,
)
package com.few.generator.core.gpt.completion

import com.google.gson.annotations.SerializedName

/**
 * Open AI API 응답 포멧
 */
data class ChatCompletion(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null,
    @SerializedName("system_fingerprint")
    val systemFingerprint: String? = null,
    val error: OpenAiError? = null,
)

data class OpenAiError(
    val code: String? = null,
    val message: String? = null,
    val type: String? = null,
    val param: String? = null,
)

data class Choice(
    val index: Int? = null,
    val message: Message? = null,
    val logprobs: Boolean? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null,
)

data class Message(
    val role: String? = null,
    val content: String? = null,
    val refusal: String? = null,
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerializedName("completion_tokens")
    val completionTokens: Int? = null,
    @SerializedName("total_tokens")
    val totalTokens: Int? = null,
    @SerializedName("prompt_tokens_details")
    val promptTokensDetails: PromptTokensDetails? = null,
    @SerializedName("completion_tokens_details")
    val completionTokensDetails: CompletionTokensDetails? = null,
)

data class PromptTokensDetails(
    @SerializedName("cached_tokens")
    val cachedTokens: Int? = null,
)

data class CompletionTokensDetails(
    @SerializedName("reasoning_tokens")
    val reasoningTokens: Int? = null,
    @SerializedName("accepted_prediction_tokens")
    val acceptedPredictionTokens: Int? = null,
    @SerializedName("rejected_prediction_tokens")
    val rejectedPredictionTokens: Int? = null,
)
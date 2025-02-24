package com.few.generator.core.gpt.completion

data class ChatCompletion(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null,
    val system_fingerprint: String? = null,
) {
    fun getFirstChoiceMessage(): String =
        choices
            ?.find { it.index == 0 }
            ?.message
            ?.content ?: throw RuntimeException("No response found in $id completion")
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
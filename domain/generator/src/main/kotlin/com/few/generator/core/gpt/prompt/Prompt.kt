package com.few.generator.core.gpt.prompt

data class Prompt(
    val model: MODEL = MODEL.GPT_4O_MINI,
    val messages: List<Message>,
    val response_format: ResponseFormat = ResponseFormat(),
    val temperature: Double = 0.2,
)

data class ResponseFormat(
    val type: String = "json_object",
)

data class Message(
    val role: ROLE,
    val content: String,
)
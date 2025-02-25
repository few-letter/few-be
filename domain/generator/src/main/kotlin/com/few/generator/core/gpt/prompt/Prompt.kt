package com.few.generator.core.gpt.prompt

import java.lang.reflect.Type

data class Prompt(
    val model: MODEL = MODEL.GPT_4O_MINI,
    val messages: List<Message>,
    val response_format: ResponseFormat,
    val temperature: Double = 0.2,
)

data class Message(
    val role: ROLE,
    val content: String,
)

data class ResponseFormat(
    val type: String = "json_object",
    val schema: Map<String, Any>,
    @Transient
    val classType: Type,
)
package com.few.generator.core.gpt.prompt

import com.few.generator.core.gpt.prompt.schema.GptResponse
import com.google.gson.annotations.SerializedName

data class Prompt(
    val model: MODEL = MODEL.GPT_4O_MINI,
    val messages: List<Message>,
    @SerializedName("response_format")
    val responseFormat: ResponseFormat,
    val temperature: Double = 0.2,
)

data class Message(
    val role: ROLE,
    val content: String,
)

data class ResponseFormat(
    val type: String = "json_schema",
    @SerializedName("json_schema")
    val jsonSchema: JsonSchema,
    @Transient
    val responseClassType: Class<out GptResponse>,
)

data class JsonSchema(
    val name: String,
    val schema: Map<String, Any>,
)
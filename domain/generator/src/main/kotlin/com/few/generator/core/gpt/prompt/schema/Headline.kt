package com.few.generator.core.gpt.prompt.schema

data class Headline(
    val headline: String,
) : GptResponse() {
    companion object {
        val name = "Headline"
        val schema =
            mapOf(
                "type" to "object",
                "properties" to
                    mapOf(
                        "headline" to
                            mapOf(
                                "type" to "string",
                            ),
                    ),
                "required" to listOf("headline"),
                "additionalProperties" to false,
            )
    }
}
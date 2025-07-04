package com.few.generator.core.gpt.prompt.schema

data class Keywords(
    val keywords: List<String>,
) : GptResponse() {
    companion object {
        val name = "Keywords"
        val schema =
            mapOf(
                "type" to "object",
                "properties" to
                    mapOf(
                        "keywords" to
                            mapOf(
                                "type" to "array",
                                "items" to
                                    mapOf(
                                        "type" to "string",
                                    ),
                            ),
                    ),
                "required" to listOf("keywords"),
                "additionalProperties" to false,
            )
    }
}
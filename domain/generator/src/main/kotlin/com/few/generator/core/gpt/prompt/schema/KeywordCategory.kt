package com.few.generator.core.gpt.prompt.schema

data class KeywordCategory(
    val keywords: List<String>,
    val category: String,
) : GptResponse() {
    companion object {
        val name = "KeywordCategory"
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
                        "category" to
                            mapOf(
                                "type" to "string",
                            ),
                    ),
                "required" to listOf("keywords"),
                "additionalProperties" to false,
            )
    }
}
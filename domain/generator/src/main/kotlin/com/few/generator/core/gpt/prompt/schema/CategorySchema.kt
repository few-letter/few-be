package com.few.generator.core.gpt.prompt.schema

data class CategorySchema(
    val category: String,
) : GptResponse() {
    companion object {
        val name = "Category"
        val schema =
            mapOf(
                "type" to "object",
                "properties" to
                    mapOf(
                        "category" to
                            mapOf(
                                "type" to "string",
                            ),
                    ),
                "required" to listOf("category"),
                "additionalProperties" to false,
            )
    }
}
package com.few.generator.core.gpt.prompt.schema

data class Summary(
    val summary: String,
) : GptResponse() {
    companion object {
        val name = "Summary"
        val schema =
            mapOf(
                "type" to "object",
                "properties" to
                    mapOf(
                        "summary" to
                            mapOf(
                                "type" to "string",
                            ),
                    ),
                "required" to listOf("summary"),
                "additionalProperties" to false,
            )
    }
}
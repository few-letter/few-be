package com.few.generator.core.gpt.prompt.schema

data class HighlightText(
    val highlightText: String,
) : GptResponse() {
    companion object {
        val name = "HighlightText"
        val schema =
            mapOf(
                "type" to "object",
                "properties" to
                    mapOf(
                        "highlightText" to
                            mapOf(
                                "type" to "string",
                            ),
                    ),
                "required" to listOf("highlightText"),
                "additionalProperties" to false,
            )
    }
}
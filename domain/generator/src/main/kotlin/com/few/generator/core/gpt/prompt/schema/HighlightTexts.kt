package com.few.generator.core.gpt.prompt.schema

data class HighlightTexts(
    val highlightTexts: List<String>,
) : GptResponse() {
    companion object {
        val schema =
            mapOf(
                "type" to "object",
                "properties" to
                    mapOf(
                        "highlightTexts" to
                            mapOf(
                                "type" to "array",
                                "items" to
                                    mapOf(
                                        "type" to "string",
                                    ),
                            ),
                    ),
                "required" to listOf("highlightTexts"),
            )
    }
}
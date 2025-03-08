package com.few.generator.core.gpt.prompt.schema

data class Texts(
    val texts: List<String>,
) : GptResponse() {
    companion object {
        val name = "Texts"
        val schema =
            mapOf(
                "type" to "object",
                "properties" to
                    mapOf(
                        "texts" to
                            mapOf(
                                "type" to "array",
                                "items" to
                                    mapOf(
                                        "type" to "string",
                                    ),
                            ),
                    ),
                "required" to listOf("texts"),
                "additionalProperties" to false,
            )
    }
}
package com.few.generator.core.gpt.prompt.schema

data class Group(
    val group: List<Int>,
) : GptResponse() {
    companion object {
        val name = "Group"
        val schema =
            mapOf(
                "type" to "object",
                "properties" to
                    mapOf(
                        "group" to
                            mapOf(
                                "type" to "array",
                                "items" to
                                    mapOf(
                                        "type" to "integer",
                                    ),
                            ),
                    ),
                "required" to listOf("group"),
                "additionalProperties" to false,
            )
    }
}
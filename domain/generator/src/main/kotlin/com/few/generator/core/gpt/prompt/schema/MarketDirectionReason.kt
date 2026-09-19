package com.few.generator.core.gpt.prompt.schema

data class MarketDirectionReason(
    val direction: String,
    val reason: String,
) : GptResponse() {
    companion object {
        val name = "MarketDirectionReason"
        val schema =
            mapOf(
                "type" to "object",
                "properties" to
                    mapOf(
                        "direction" to
                            mapOf(
                                "type" to "string",
                            ),
                        "reason" to
                            mapOf(
                                "type" to "string",
                            ),
                    ),
                "required" to listOf("direction", "reason"),
                "additionalProperties" to false,
            )
    }
}
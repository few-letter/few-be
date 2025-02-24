package com.few.generator.core.gpt.prompt

enum class MODEL(
    val value: String,
) {
    GPT_4O("gpt-4o"),
    FOURO_MINI("gpt-4o-mini"),

    ;

    companion object {
        fun fromValue(value: String): MODEL? = entries.find { it.value == value }
    }
}
package com.few.generator.core.gpt.prompt

enum class ROLE(
    val value: String,
) {
    SYSTEM("system"),
    USER("user"),

    ;

    companion object {
        fun fromValue(value: String): ROLE? = entries.find { it.value == value }
    }
}
package com.few.generator.core.prompt

data class Prompt(
    val systemPrompt: String,
    val userPrompt: String,
    val jsonObject: Map<String, Any>,
)
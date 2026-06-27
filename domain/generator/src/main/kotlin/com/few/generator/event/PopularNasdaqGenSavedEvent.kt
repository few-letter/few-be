package com.few.generator.event

data class PopularNasdaqGenSavedEvent(
    val genIdsByStock: Map<String, List<Long>>,
)
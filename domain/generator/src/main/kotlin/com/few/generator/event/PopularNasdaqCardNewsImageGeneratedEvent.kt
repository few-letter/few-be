package com.few.generator.event

data class PopularNasdaqCardNewsImageGeneratedEvent(
    val imagePathsByStock: Map<String, List<String>>,
    val mainPageImagePathsByStock: Map<String, String>,
)
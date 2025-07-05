package com.few.generator.domain.vo

import com.few.generator.domain.Gen
import java.util.concurrent.CompletableFuture

data class AsyncKeywordExtraction(
    val gen: Gen,
    val keywordFuture: CompletableFuture<String>,
)
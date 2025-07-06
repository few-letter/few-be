package com.few.generator.domain.vo

import com.few.generator.domain.Gen
import kotlinx.coroutines.Deferred

data class AsyncKeywordJob(
    val gen: Gen,
    val keywordDeferred: Deferred<String>,
)
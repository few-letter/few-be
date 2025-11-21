package com.few.generator.domain.vo

import com.few.generator.domain.Gen
import java.time.LocalDate

data class NewsletterData(
    val targetDate: LocalDate,
    val gensByCategory: Map<Int, List<Gen>>,
    val rawContentsUrlsByGens: Map<Long, String>,
    val rawContentsMediaTypeNameByGens: Map<Long, String>,
) {
    fun isEmpty(): Boolean = gensByCategory.isEmpty()
}
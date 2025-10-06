package com.few.generator.service.implement

import com.few.common.domain.Category
import com.few.email.GenData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class NewsletterTemplateData(
    val date: String,
    val gensByCategory: List<CategoryGenData>,
) {
    companion object {
        fun from(
            date: LocalDate,
            gensByCategory: Map<Int, List<GenData>>,
        ): NewsletterTemplateData {
            val formatter = DateTimeFormatter.ofPattern("yyyy. M. d")
            val categoryGenDataList =
                gensByCategory.map { (categoryCode, gens) ->
                    CategoryGenData(
                        categoryName = Category.from(categoryCode).title,
                        gens = gens,
                        categoryCode = categoryCode,
                        categoryEmoji = emoji(Category.from(categoryCode)),
                    )
                }

            return NewsletterTemplateData(
                date = date.format(formatter),
                gensByCategory = categoryGenDataList,
            )
        }

        fun emoji(category: Category): String =
            when (category) {
                Category.TECHNOLOGY -> "\uD83D\uDCBB" // 💻
                Category.LIFE -> "\uD83C\uDFE1" // 🏡
                Category.POLITICS -> "\uD83C\uDFDB\uFE0F" // 🏛️
                Category.ECONOMY -> "\uD83D\uDCB0" // 💰
                Category.SOCIETY -> "\uD83C\uDF0E" // 🌎
                else -> "\uD83D\uDD2E" // 🔮
            }
    }
}

data class CategoryGenData(
    val categoryCode: Int,
    val categoryName: String,
    val gens: List<GenData>,
    val categoryEmoji: String,
)
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
                    )
                }

            return NewsletterTemplateData(
                date = date.format(formatter),
                gensByCategory = categoryGenDataList,
            )
        }
    }
}

data class CategoryGenData(
    val categoryName: String,
    val gens: List<GenData>,
)
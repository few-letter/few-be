package com.few.generator.service.specifics.newsletter

import com.few.common.domain.Category
import com.few.email.CategoryModel
import com.few.email.GenModel
import com.few.email.NewsletterModel
import com.few.generator.config.NewsletterProperties
import com.few.generator.domain.Gen
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class NewsletterModelGenerator(
    private val properties: NewsletterProperties,
) {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy. M. d")

    fun generate(
        date: LocalDate,
        gens: List<Gen>,
        userEmail: String,
        rawContentsUrlsByGens: Map<Long, String>,
        rawContentsMediaTypeNameByGens: Map<Long, String>,
    ): NewsletterModel {
        val genModelList =
            gens.map { gen ->
                GenModel(
                    id = gen.id!!,
                    headline = gen.headline,
                    summary = gen.summary,
                    category = gen.category,
                    url = rawContentsUrlsByGens[gen.id!!]!!,
                    mediaTypeName = rawContentsMediaTypeNameByGens[gen.id!!]!!,
                )
            }

        val gensByCategory = genModelList.groupBy { it.category }

        val categoryModelList =
            gensByCategory.map { (categoryCode, gens) ->
                CategoryModel(
                    categoryName = Category.from(categoryCode).title,
                    gens = gens,
                    categoryCode = categoryCode,
                    categoryEmoji = emoji(Category.from(categoryCode)),
                )
            }

        return NewsletterModel(
            dateString = formatter.format(date),
            gensByCategory = categoryModelList,
            userEmail = userEmail,
            landingPageUrl = properties.landingPageUrl,
            unsubscribeUrl = "${properties.unsubscribePageUrl}?email=$userEmail",
            instagramUrl = properties.instagramUrl,
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
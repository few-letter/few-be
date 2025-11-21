package com.few.generator.service.specifics.newsletter

import com.few.common.domain.Category
import com.few.generator.config.NewsletterProperties
import com.few.generator.domain.Gen
import com.few.generator.domain.Subscription
import org.springframework.stereotype.Service

@Service
class NewsletterBusinessRules(
    private val properties: NewsletterProperties,
) {
    fun generateSubject(
        subscription: Subscription,
        gensByCategory: Map<Int, List<Gen>>,
    ): String {
        val categories = Category.parseCategories(subscription.categories)
        val firstCategoryCode = categories.firstOrNull()?.code ?: return "${properties.subjectPrefix} 뉴스"
        val firstGen = gensByCategory[firstCategoryCode]?.firstOrNull()
        val headline = firstGen?.headline ?: "뉴스"
        return "${properties.subjectPrefix} $headline"
    }

    fun shouldSendNewsletter(
        subscription: Subscription,
        availableGens: Map<Int, List<Gen>>,
    ): Boolean {
        val categories = Category.parseCategories(subscription.categories)
        val todayGens = categories.flatMap { category -> availableGens[category.code].orEmpty() }
        return todayGens.isNotEmpty()
    }

    fun getRelevantGens(
        subscription: Subscription,
        gensByCategory: Map<Int, List<Gen>>,
    ): List<Gen> {
        val categories = Category.parseCategories(subscription.categories)
        return categories.flatMap { category -> gensByCategory[category.code].orEmpty() }
    }
}
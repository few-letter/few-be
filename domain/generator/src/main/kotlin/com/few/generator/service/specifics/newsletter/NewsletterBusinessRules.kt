package com.few.generator.service.specifics.newsletter

import com.few.generator.config.NewsletterProperties
import com.few.generator.domain.Gen
import com.few.generator.domain.Subscription
import com.few.generator.domain.vo.Categories
import org.springframework.stereotype.Service

@Service
class NewsletterBusinessRules(
    private val properties: NewsletterProperties,
) {
    fun generateSubject(
        subscription: Subscription,
        gensByCategory: Map<Int, List<Gen>>,
    ): String {
        val categories = Categories.from(subscription.categories)
        val firstCategory = categories.values.firstOrNull() ?: return "${properties.subjectPrefix} 뉴스"
        val firstGen = gensByCategory[firstCategory]?.firstOrNull()
        val headline = firstGen?.headline ?: "뉴스"
        return "${properties.subjectPrefix} $headline"
    }

    fun shouldSendNewsletter(
        subscription: Subscription,
        availableGens: Map<Int, List<Gen>>,
    ): Boolean {
        val categories = Categories.from(subscription.categories)
        val todayGens = categories.values.flatMap { category -> availableGens[category].orEmpty() }
        return todayGens.isNotEmpty()
    }

    fun getRelevantGens(
        subscription: Subscription,
        gensByCategory: Map<Int, List<Gen>>,
    ): List<Gen> {
        val categories = Categories.from(subscription.categories)
        return categories.values.flatMap { category -> gensByCategory[category].orEmpty() }
    }
}
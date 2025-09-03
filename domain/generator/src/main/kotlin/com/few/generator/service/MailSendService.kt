package com.few.generator.service

import com.few.email.GenData
import com.few.email.GenNewsletterArgs
import com.few.email.GenNewsletterContent
import com.few.email.GenNewsletterSender
import com.few.generator.domain.Subscription
import com.few.generator.repository.GenRepository
import com.few.generator.repository.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class MailSendService(
    private val subscriptionRepository: SubscriptionRepository,
    private val genRepository: GenRepository,
    private val genNewsletterSender: GenNewsletterSender,
    private val dateProvider: DateProvider,
    private val newsletterContentBuilder: NewsletterContentBuilder,
) {
    private val log = KotlinLogging.logger {}
    private val pageSize = 100

    fun sendDailyNewsletter(): Pair<Int, Int> {
        val dateRange = getTodayDateRange()
        val genCache = GenCache(genRepository, dateRange)
        var successCount = 0
        var failCount = 0
        var page = 0

        do {
            val subscriptionPage = subscriptionRepository.findAll(PageRequest.of(page, pageSize))

            subscriptionPage.content.forEach { subscription ->
                if (sendNewsletterToSubscriber(subscription, genCache)) {
                    successCount++
                } else {
                    failCount++
                }
            }

            page++
        } while (subscriptionPage.hasNext())

        return successCount to failCount
    }

    private fun getTodayDateRange(): DateRange {
        val targetDate = dateProvider.getTargetDate()
        return DateRange(targetDate.atStartOfDay(), targetDate.plusDays(1).atStartOfDay())
    }

    private fun sendNewsletterToSubscriber(
        subscription: Subscription,
        genCache: GenCache,
    ): Boolean {
        val categories = parseCategories(subscription.categories)
        val todayGens = genCache.getGensByCategories(categories)

        if (todayGens.isEmpty()) return true

        return runCatching {
            val genDataList =
                todayGens.map { gen ->
                    GenData(gen.id!!, gen.headline, gen.summary, gen.category)
                }

            val targetDate = dateProvider.getTargetDate()
            val gensByCategory = genDataList.groupBy { it.category }
            val emailContext = newsletterContentBuilder.buildEmailContext(targetDate, gensByCategory)

            val newsletterArgs =
                GenNewsletterArgs(
                    to = subscription.email,
                    subject = "FEW Letter - $targetDate 뉴스레터",
                    content = GenNewsletterContent(genDataList),
                    emailContext = emailContext,
                )

            genNewsletterSender.send(newsletterArgs)
            true
        }.getOrElse { ex ->
            log.error(ex) { "메일 발송 실패 - 구독자: ${subscription.email}" }
            false
        }
    }

    private fun parseCategories(categories: String): List<Int> = categories.split(",").mapNotNull { it.trim().toIntOrNull() }
}
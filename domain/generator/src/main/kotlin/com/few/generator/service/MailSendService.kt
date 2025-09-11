package com.few.generator.service

import com.few.email.GenData
import com.few.email.GenNewsletterArgs
import com.few.email.GenNewsletterContent
import com.few.email.GenNewsletterSender
import com.few.generator.domain.Gen
import com.few.generator.domain.Subscription
import com.few.generator.repository.GenRepository
import com.few.generator.repository.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate

@Service
class MailSendService(
    private val subscriptionRepository: SubscriptionRepository,
    private val genRepository: GenRepository,
    private val genNewsletterSender: GenNewsletterSender,
    private val newsletterContentBuilder: NewsletterContentBuilder,
    private val genUrlService: GenUrlService,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val log = KotlinLogging.logger {}
    private val pageSize = 100

    fun sendDailyNewsletter(): Pair<Int, Int> {
        val latestGenDate =
            genRepository
                .findFirstLimit(1)
                .firstOrNull()
                ?.createdAt
                ?.toLocalDate()
        val today = LocalDate.now(clock)

        if (latestGenDate == null || latestGenDate.isBefore(today)) {
            return 0 to 0
        }

        val dateRange = DateRange(latestGenDate.atStartOfDay(), latestGenDate.plusDays(1).atStartOfDay())
        val gensToSend = genRepository.findAllByCreatedAtBetween(dateRange.start, dateRange.end)
        val gensByCategory = gensToSend.groupBy { it.category }
        val rawContentsUrlsByGens = genUrlService.getRawContentsUrlsByGens(gensToSend)
        var successCount = 0
        var failCount = 0
        var page = 0

        do {
            val subscriptionPage = subscriptionRepository.findAll(PageRequest.of(page, pageSize))

            subscriptionPage.content.forEach { subscription ->
                if (sendNewsletterToSubscriber(subscription, gensByCategory, rawContentsUrlsByGens, latestGenDate)) {
                    successCount++
                } else {
                    failCount++
                }
            }

            page++
        } while (subscriptionPage.hasNext())

        return successCount to failCount
    }

    private fun sendNewsletterToSubscriber(
        subscription: Subscription,
        gensByCategory: Map<Int, List<Gen>>,
        rawContentsUrlsByGens: Map<Long, String>,
        targetDate: java.time.LocalDate,
    ): Boolean {
        val categories = parseCategories(subscription.categories)
        val todayGens = categories.flatMap { category -> gensByCategory[category].orEmpty() }

        if (todayGens.isEmpty()) return true

        return runCatching {
            val genDataList =
                todayGens.map { gen ->
                    GenData(
                        id = gen.id!!,
                        headline = gen.headline,
                        summary = gen.summary,
                        category = gen.category,
                        url = rawContentsUrlsByGens[gen.id!!],
                    )
                }

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
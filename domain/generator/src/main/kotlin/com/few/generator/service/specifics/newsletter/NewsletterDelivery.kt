package com.few.generator.service.specifics.newsletter

import com.few.email.NewsletterEmailRenderer
import com.few.email.provider.EmailSendProvider
import com.few.generator.config.NewsletterProperties
import com.few.generator.domain.Subscription
import com.few.generator.domain.vo.NewsletterData
import com.few.generator.domain.vo.SendResult
import com.few.generator.service.SubscriptionService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class NewsletterDelivery(
    private val subscriptionService: SubscriptionService,
    private val emailSendProvider: EmailSendProvider,
    private val newsletterModelGenerator: NewsletterModelGenerator,
    private val newsletterEmailRenderer: NewsletterEmailRenderer,
    private val newsletterBusinessRules: NewsletterBusinessRules,
    private val properties: NewsletterProperties,
) {
    private val log = KotlinLogging.logger {}

    fun sendToSubscribers(data: NewsletterData): SendResult {
        if (data.isEmpty()) return SendResult(0, 0)

        var totalResult = SendResult(0, 0)
        var page = 0

        do {
            val subscriptionPage = subscriptionService.findAll(PageRequest.of(page, properties.pageSize))

            subscriptionPage.content.forEach { subscription ->
                val result = sendNewsletterToSubscriber(subscription, data)
                totalResult += result
            }

            page++
        } while (subscriptionPage.hasNext())

        return totalResult
    }

    private fun sendNewsletterToSubscriber(
        subscription: Subscription,
        data: NewsletterData,
    ): SendResult {
        if (!newsletterBusinessRules.shouldSendNewsletter(subscription, data.gensByCategory)) {
            return SendResult(0, 0) // 발송할 콘텐츠가 없으면 카운트하지 않음
        }

        return runCatching {
            val relevantGens = newsletterBusinessRules.getRelevantGens(subscription, data.gensByCategory)

            val templateModel =
                newsletterModelGenerator.generate(
                    date = data.targetDate,
                    gens = relevantGens,
                    userEmail = subscription.email,
                    rawContentsUrlsByGens = data.rawContentsUrlsByGens,
                    rawContentsMediaTypeNameByGens = data.rawContentsMediaTypeNameByGens,
                )

            val message = newsletterEmailRenderer.render(templateModel)
            val subject = newsletterBusinessRules.generateSubject(subscription, data.gensByCategory)

            emailSendProvider.sendEmail(
                from = properties.fromEmail,
                to = subscription.email,
                subject = subject,
                message = message,
            )

            SendResult(1, 0)
        }.getOrElse { ex ->
            log.error(ex) { "메일 발송 실패 - 구독자: ${subscription.email}" }
            SendResult(0, 1)
        }
    }
}
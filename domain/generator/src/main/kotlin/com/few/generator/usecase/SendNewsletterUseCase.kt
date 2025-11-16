package com.few.generator.usecase

import com.few.generator.domain.vo.SendResult
import com.few.generator.service.GenService
import com.few.generator.service.specifics.newsletter.NewsletterContentAggregator
import com.few.generator.service.specifics.newsletter.NewsletterDelivery
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class SendNewsletterUseCase(
    private val genService: GenService,
    private val newsletterContentAggregator: NewsletterContentAggregator,
    private val newsletterDelivery: NewsletterDelivery,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun execute(): SendResult {
        val latestGenDate =
            genService.findLatestGen().createdAt?.toLocalDate()
                ?: return SendResult(0, 0)

        val newsletterData = newsletterContentAggregator.prepareNewsletterData(latestGenDate)
        return newsletterDelivery.sendToSubscribers(newsletterData)
    }
}
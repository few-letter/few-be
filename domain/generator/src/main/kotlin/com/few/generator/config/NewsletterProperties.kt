package com.few.generator.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("newsletter")
data class NewsletterProperties(
    var pageSize: Int = 100,
    var fromEmail: String = "noreply@fewletter.store",
    var subjectPrefix: String = "[국내 뉴스]",
    var landingPageUrl: String = "https://www.few-letter.com",
    var unsubscribePageUrl: String = "$landingPageUrl/delete-subscription",
    var instagramUrl: String = "https://www.instagram.com/few.letter",
)
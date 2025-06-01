package email.provider

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import org.springframework.stereotype.Component

@Component
class ArticleAwsSESEmailSendProvider(
    amazonSimpleEmailService: AmazonSimpleEmailService,
) : AwsSESEmailSendProvider(
        amazonSimpleEmailService,
    ) {
    override fun getWithConfigurationSetName(): String = "few-article-configuration-set"
}
package email.provider

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class AwsSendEmailServiceProvider(
    private val amazonSimpleEmailService: AmazonSimpleEmailService,
) : EmailSendProvider {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val UTF_8 = "utf-8"
    }

    override fun sendEmail(
        from: String,
        to: String,
        subject: String,
        message: String,
    ): String {
        val destination = Destination().withToAddresses(to)
        val sendMessage =
            Message()
                .withSubject(Content().withCharset(UTF_8).withData(subject))
                .withBody(Body().withHtml(Content().withCharset(UTF_8).withData(message)))

        val sendEmailRequest =
            SendEmailRequest()
                .withSource(from)
                .withDestination(destination)
                .withMessage(sendMessage)
                .withConfigurationSetName(getWithConfigurationSetName())

        runCatching {
            amazonSimpleEmailService.sendEmail(sendEmailRequest).messageId
        }.onFailure {
            log.warn {
                "Failed to send email using AWS SES. Falling back to JavaMailSender. Error: $it"
            }
        }.let {
            return it.getOrThrow()
        }
    }

    /**
     * Default configuration set name is "few-configuration-set"
     */
    fun getWithConfigurationSetName(): String = "ses-configuration-set"
}
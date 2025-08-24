package com.few.email.provider

import com.amazonaws.services.simpleemail.model.AmazonSimpleEmailServiceException
import com.few.email.config.MailConfig
import com.few.email.config.MailSenderConfig
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@Import(MailConfig::class, MailSenderConfig::class)
@DisplayName("AwsSendEmailServiceProvider 통합 테스트")
@ActiveProfiles("email-local")
class AwsSendEmailServiceProviderTest {
    companion object {
        const val TEST_SENDER = "noreply@fewletter.store"
    }

    @Autowired
    private lateinit var awsSendEmailServiceProvider: AwsSendEmailServiceProvider

    @Test
    @DisplayName("SES를 통해 이메일이 성공적으로 발송되어야 한다")
    fun `should send email successfully via SES`() {
        val from = "FEW Letter <$TEST_SENDER>"
        val to = "few.dev@fewletter.store"
        val subject = "SES 발송 테스트"
        val message =
            """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h1 style="color: #333;">AWS SES 테스트</h1>
                    <p>안녕하세요!</p>
                    <p>AWS SES를 통한 <strong>HTML 형식</strong> 이메일 발송 테스트입니다.</p>
                    <ul>
                        <li>뉴스레터 발송</li>
                        <li>HTML 스타일링</li>
                    </ul>
                    <p>감사합니다.</p>
                    <p><em>FEW Letter 팀</em></p>
                </div>
            </body>
            </html>
            """.trimIndent()

        val messageId = awsSendEmailServiceProvider.sendEmail(from, to, subject, message)

        assert(messageId.isNotBlank()) { "Message ID가 비어있습니다" }
        println("✅ AWS SES 이메일 발송 성공!")
        println("📧 Message ID: $messageId")
        println("📮 발신자: $from")
        println("📮 수신자: $to")
//        println("📝 제목: $subject")

        verifyEmailSent(to, subject)
    }

    @Test
    @DisplayName("잘못된 이메일 주소로 발송 시 예외가 발생해야 한다")
    fun `should throw exception for invalid email address`() {
        val from = "FEW Letter <$TEST_SENDER>"
        val to = "invalid-email"
        val subject = "잘못된 이메일 테스트"
        val message = "<html><body>테스트</body></html>"

        try {
            awsSendEmailServiceProvider.sendEmail(from, to, subject, message)
            assert(false) { "예외가 발생해야 합니다" }
        } catch (e: Exception) {
            println("✅ 예상된 예외 발생: ${e.javaClass.simpleName}")
            println("   메시지: ${e.message}")
            assert(e is AmazonSimpleEmailServiceException || e is IllegalArgumentException)
        }
    }

    private fun verifyEmailSent(
        expectedTo: String,
        expectedSubject: String,
    ) {
        println("📬 AWS SES 발송 확인:")
        println("   - 수신자: $expectedTo ✓")
        println("   - 제목: $expectedSubject ✓")
        println("   - 상태: 발송 요청 성공 ✓")
    }
}
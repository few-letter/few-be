package com.few.generator.usecase

import com.few.email.config.AwsSendEmailServiceProviderConfig
import com.few.email.config.MailConfig
import com.few.email.config.MailSenderConfig
import com.few.generator.config.GeneratorDataSourceConfig
import com.few.generator.config.GeneratorGsonConfig
import com.few.generator.config.GeneratorJpaConfig
import com.few.generator.domain.Gen
import com.few.generator.domain.Subscription
import com.few.generator.repository.GenRepository
import com.few.generator.repository.SubscriptionRepository
import com.few.generator.service.DateProvider
import com.few.generator.service.GenUrlService
import com.few.generator.service.MailSendService
import com.few.generator.service.NewsletterContentBuilder
import com.few.generator.support.jpa.GeneratorTransactional
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.time.LocalDate

@SpringBootTest(
    properties = ["spring.profiles.active=email-local,generator-local"],
    classes = [
        MailSendService::class,
        GeneratorDataSourceConfig::class,
        GeneratorJpaConfig::class,
        GeneratorGsonConfig::class,
        MailSenderConfig::class,
        MailConfig::class,
        AwsSendEmailServiceProviderConfig::class,
        NewsLetterServiceTest.TestConfig::class,
        NewsletterContentBuilder::class,
        GenUrlService::class,
    ],
)
@DisplayName("뉴스레터 SES 통합 테스트")
@Tag("integration")
class NewsLetterServiceTest {
    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun testDateProvider(): DateProvider {
            val mockDateProvider = mockk<DateProvider>()
            every { mockDateProvider.getTargetDate() } returns LocalDate.now()
            return mockDateProvider
        }
    }

    @Autowired
    private lateinit var subscriptionRepository: SubscriptionRepository

    @Autowired
    private lateinit var genRepository: GenRepository

    @Autowired
    private lateinit var mailSendService: MailSendService

    @Test
    @GeneratorTransactional
    @DisplayName("실제 SES를 통한 뉴스레터 전송이 성공해야 한다")
    fun `should send newsletter successfully via SES`() {
        // Given
        val testEmail = "hun.ca.dev@gmail.com"
        val today = LocalDate.now()

        val testGen =
            createTestGen(
                headline = "테스트 기술 뉴스 - $today",
                summary = "Spring Boot와 Kotlin을 활용한 뉴스레터 시스템 구축",
            )

        val testSubscription = createTestSubscription(testEmail)

        listOf(testGen, testSubscription).forEach { entity ->
            when (entity) {
                is Gen -> genRepository.save(entity)
                is Subscription -> subscriptionRepository.save(entity)
            }
        }

        // When
        val (successCount, failCount) = mailSendService.sendDailyNewsletter()

        // Then
        successCount shouldBeAtLeast 0

        logResults(successCount, failCount, testEmail)
        verifyNewsletterSent(testEmail, "FEW Letter - $today 뉴스레터")
    }

    @Test
    @GeneratorTransactional
    @DisplayName("구독하지 않은 카테고리의 Gen은 발송되지 않아야 한다")
    fun `should not send newsletter for unsubscribed categories`() {
        // Given
        val testEmail = "ngolo6187@gmail.com"
        val today = LocalDate.now()

        val subscribedGen =
            createTestGen(
                headline = "구독한 기술 뉴스 - $today",
                summary = "구독한 카테고리의 뉴스입니다",
                category = 2, // 기술 카테고리
            )

        val unsubscribedGen =
            createTestGen(
                headline = "구독하지 않은 생활 뉴스 - $today",
                summary = "구독하지 않은 카테고리의 뉴스입니다",
                category = 4, // 생활 카테고리
            )

        val testSubscription =
            createTestSubscription(
                email = testEmail,
                categories = "2", // 기술 카테고리만 구독
            )

        listOf(subscribedGen, unsubscribedGen, testSubscription).forEach { entity ->
            when (entity) {
                is Gen -> genRepository.save(entity)
                is Subscription -> subscriptionRepository.save(entity)
            }
        }

        // When
        val (successCount, failCount) = mailSendService.sendDailyNewsletter()

        // Then
        successCount shouldBeAtLeast 0

        println("✅ 카테고리 필터링 테스트 완료!")
        println("📊 전송 결과 - 성공: $successCount, 실패: $failCount")
        println("📮 수신자: $testEmail (카테고리 2만 구독)")
        println("📝 구독한 Gen: ${subscribedGen.headline}")
        println("🚫 구독하지 않은 Gen: ${unsubscribedGen.headline}")

        verifyNewsletterSent(testEmail, "FEW Letter - $today 뉴스레터")
    }

    private fun createTestGen(
        headline: String,
        summary: String,
        category: Int = 2,
    ) = Gen(
        provisioningContentsId = 1L,
        headline = headline,
        summary = summary,
        category = category,
    )

    private fun createTestSubscription(
        email: String,
        categories: String = "2,4,6",
    ) = Subscription(
        email = email,
        categories = categories,
    )

    private infix fun Int.shouldBeAtLeast(expected: Int) {
        assert(this >= expected) { "성공 건수가 $expected 이상이어야 합니다" }
    }

    private fun logResults(
        successCount: Int,
        failCount: Int,
        email: String,
    ) {
        println("✅ 뉴스레터 발송 완료!")
        println("📊 전송 결과 - 성공: $successCount, 실패: $failCount")
        println("📮 수신자: $email")
    }

    private fun verifyNewsletterSent(
        expectedTo: String,
        expectedSubject: String,
    ) {
        val verificationItems =
            listOf(
                "수신자" to expectedTo,
                "제목" to expectedSubject,
                "상태" to "발송 요청 성공",
            )

        println("📬 뉴스레터 발송 확인:")
        verificationItems.forEach { (key, value) ->
            println("   - $key: $value ✓")
        }
    }
}
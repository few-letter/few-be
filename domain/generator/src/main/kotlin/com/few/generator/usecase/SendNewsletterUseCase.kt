package com.few.generator.usecase

import com.few.email.GenData
import com.few.email.GenNewsletterArgs
import com.few.email.GenNewsletterContent
import com.few.email.GenNewsletterSender
import com.few.generator.domain.Gen
import com.few.generator.domain.Subscription
import com.few.generator.repository.SubscriptionRepository
import com.few.generator.service.GenService
import com.few.generator.service.GenUrlService
import com.few.generator.service.NewsletterContentBuilder
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

@Component
class SendNewsletterUseCase(
    private val subscriptionRepository: SubscriptionRepository, // 설계 원칙 위배
    private val genService: GenService, // 설계 원칙 위배
    private val genNewsletterSender: GenNewsletterSender, // 설계 원칙 위배
    private val newsletterContentBuilder: NewsletterContentBuilder,
    private val genUrlService: GenUrlService, // 설계 원칙 위배
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)
    private val pageSize = 100

    @Scheduled(cron = "0 0 8 * * *")
    @GeneratorTransactional
    fun execute() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "뉴스레터 스케줄링이 이미 실행 중입니다." }
            return
        }

        try {
            doExecute()
        } finally {
            isRunning.set(false)
        }
    }

    private fun doExecute() {
        val startTime = LocalDateTime.now()
        var isSuccess = true
        var executionTimeSec = 0.0
        var exception: Throwable? = null

        var result: Pair<Int, Int> = Pair(0, 0)

        runCatching {
            executionTimeSec =
                measureTimeMillis {
                    result = sendDailyNewsletter()
                }.msToSeconds()
        }.onFailure { ex ->
            isSuccess = false
            log.error(ex) { "뉴스레터 전송 중 오류 발생" }
            exception = ex
        }.also {
            log.info {
                buildString {
                    appendLine("📧 뉴스레터 전송 완료")
                    appendLine("✅ 성공 여부: $isSuccess")
                    appendLine("✅ 시작 시간: $startTime")
                    appendLine("✅ 소요 시간: ${executionTimeSec}초")
                    appendLine("✅ 결과: 성공(${result.first}) / 실패(${result.second})")
                    if (!isSuccess) appendLine("❌ 오류: ${exception?.message}")
                }
            }
        }
    }

    private fun sendDailyNewsletter(): Pair<Int, Int> {
        val latestGenDate = genService.findLatestGen().createdAt ?: return 0 to 0
        val today = LocalDateTime.now(clock)
        if (latestGenDate.isBefore(today)) {
            return 0 to 0
        }

        val gensToSend =
            genService.findAllByCreatedAtBetween(
                start = latestGenDate.toLocalDate().atStartOfDay(),
                end = latestGenDate.toLocalDate().plusDays(1).atStartOfDay(),
            )

        val gensByCategory = gensToSend.groupBy { it.category }
        val rawContentsUrlsByGens = genUrlService.getRawContentsUrlsByGens(gensToSend)
        var successCount = 0
        var failCount = 0
        var page = 0

        do {
            val subscriptionPage = subscriptionRepository.findAll(PageRequest.of(page, pageSize))

            subscriptionPage.content.forEach { subscription ->
                if (sendNewsletterToSubscriber(subscription, gensByCategory, rawContentsUrlsByGens, latestGenDate.toLocalDate())) {
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

    private fun Long.msToSeconds(): Double = this / 1000.0
}
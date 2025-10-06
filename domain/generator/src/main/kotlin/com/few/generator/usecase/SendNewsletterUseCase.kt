package com.few.generator.usecase

import com.few.common.domain.MediaType
import com.few.email.GenData
import com.few.email.GenNewsletterArgs
import com.few.email.GenNewsletterContent
import com.few.email.GenNewsletterSender
import com.few.generator.domain.Gen
import com.few.generator.domain.RawContents
import com.few.generator.domain.Subscription
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.service.SubscriptionService
import com.few.generator.service.specifics.newsletter.NewsletterContentBuilder
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

@Component
class SendNewsletterUseCase(
    private val subscriptionService: SubscriptionService,
    private val genService: GenService,
    private val provisioningService: ProvisioningService,
    private val rawContentsService: RawContentsService,
    private val genNewsletterSender: GenNewsletterSender,
    private val newsletterContentBuilder: NewsletterContentBuilder,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)
    private val pageSize = 100

    @Scheduled(cron = "\${scheduling.cron.send}", zone = "Asia/Seoul")
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

        val gensToSend =
            genService.findAllByCreatedAtBetween(
                start = latestGenDate.toLocalDate().atStartOfDay(),
                end = latestGenDate.toLocalDate().plusDays(1).atStartOfDay(),
            )

        val gensByCategory = gensToSend.groupBy { it.category }
        val rawContentsByGenId = getRawContentsByGens(gensToSend)
        val rawContentsUrlsByGens = getRawContentsUrlsByGens(rawContentsByGenId)
        val rawContentsMediaTypeByGens = getRawContentsMediaTypeNameByGens(rawContentsByGenId)
        var successCount = 0
        var failCount = 0
        var page = 0

        do {
            val subscriptionPage = subscriptionService.findAll(PageRequest.of(page, pageSize))

            subscriptionPage.content.forEach { subscription ->
                if (sendNewsletterToSubscriber(
                        subscription = subscription,
                        gensByCategory = gensByCategory,
                        rawContentsUrlsByGens = rawContentsUrlsByGens,
                        rawContentsMediaTypeNameByGens = rawContentsMediaTypeByGens,
                        targetDate = latestGenDate.toLocalDate(),
                    )
                ) {
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
        rawContentsMediaTypeNameByGens: Map<Long, String>,
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
                        url = rawContentsUrlsByGens[gen.id!!]!!,
                        mediaTypeName = rawContentsMediaTypeNameByGens[gen.id!!]!!,
                    )
                }

            val gensByCategory = genDataList.groupBy { it.category }
            val emailContext = newsletterContentBuilder.buildEmailContext(targetDate, gensByCategory)

            val newsletterArgs =
                GenNewsletterArgs(
                    to = subscription.email,
                    subject = getSubject(subscription, gensByCategory),
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

    fun getSubject(
        subscription: Subscription,
        gensByCategory: Map<Int, List<GenData>>,
    ): String {
        val firstCategory = parseCategories(subscription.categories)[0]
        val firstContentOfFirstCategory = gensByCategory[firstCategory]?.get(0)
        val headline = firstContentOfFirstCategory?.headline ?: ""
        return "[국내 뉴스] $headline"
    }

    private fun getRawContentsUrlsByGens(rawContentsById: Map<Long, RawContents>): Map<Long, String> =
        rawContentsById
            .mapNotNull { it ->
                val genId = it.key
                val rawContents = it.value
                genId to rawContents.url
            }.toMap()

    private fun getRawContentsMediaTypeNameByGens(rawContentsById: Map<Long, RawContents>): Map<Long, String> =
        rawContentsById
            .mapNotNull { it ->
                val genId = it.key
                val rawContents = it.value
                val mediaType = MediaType.from(rawContents.mediaType)
                genId to mediaType.title
            }.toMap()

    private fun getRawContentsByGens(gens: List<Gen>): Map<Long, RawContents> {
        if (gens.isEmpty()) return emptyMap()

        val provisioningIds = gens.map { it.provisioningContentsId }.distinct()
        if (provisioningIds.isEmpty()) return emptyMap()
        val provisioningContents = provisioningService.findAllByIdIn(provisioningIds)
        val provisioningById =
            provisioningContents
                .mapNotNull { provisioning -> provisioning.id?.let { provisioning.id to provisioning } }
                .toMap()

        val rawContentsIds = provisioningContents.map { it.rawContentsId }.distinct()
        if (rawContentsIds.isEmpty()) return emptyMap()
        val rawContents = rawContentsService.findAllByIdIn(rawContentsIds)
        val rawContentsById =
            rawContents
                .mapNotNull { rawContent -> rawContent.id?.let { it to rawContent } }
                .toMap()

        val rawContentsByGenId =
            gens
                .mapNotNull { it ->
                    val pId = it.provisioningContentsId
                    val rawId = provisioningById[pId]?.rawContentsId ?: return@mapNotNull null
                    val rawContents = rawContentsById[rawId]
                    rawContents?.let { rawContents -> it.id?.let { it to rawContents } }
                }.toMap()

        return rawContentsByGenId
    }

    private fun parseCategories(categories: String): List<Int> = categories.split(",").mapNotNull { it.trim().toIntOrNull() }

    private fun Long.msToSeconds(): Double = this / 1000.0
}
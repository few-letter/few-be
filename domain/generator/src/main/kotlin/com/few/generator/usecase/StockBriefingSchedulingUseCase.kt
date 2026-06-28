package com.few.generator.usecase

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.core.instagram.StockBriefingContent
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.event.StockBriefingContentProcessedEvent
import com.few.generator.event.StockBriefingInstagramUploadCompletedEvent
import com.few.generator.service.StockBriefingPostStateService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Component
class StockBriefingSchedulingUseCase(
    private val scrapper: Scrapper,
    private val chatGpt: ChatGpt,
    private val promptGenerator: PromptGenerator,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val stockBriefingPostStateService: StockBriefingPostStateService,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    @Async("generatorSchedulingExecutor")
    fun executeAsync() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "증시 브리핑 스케줄링이 이미 실행 중입니다." }
            return
        }

        try {
            execute()
        } catch (e: Exception) {
            log.error(e) { "증시 브리핑 스케줄링 실행 중 오류: ${e.message}" }
        } finally {
            isRunning.set(false)
        }
    }

    fun execute() {
        val today = LocalDate.now().toString()
        val nextPostId =
            scrapper.fetchStockBriefingLatestPostId(today)
                ?: throw RuntimeException("증시 브리핑 최신 포스트 ID를 가져오지 못했습니다. (date=$today)")
        log.info { "증시 브리핑 최신 포스트 확인 (postId=$nextPostId)" }

        val rawContents =
            try {
                scrapper.scrapeStockBriefingPost(nextPostId)
            } catch (e: Exception) {
                log.error(e) { "증시 브리핑 크롤링 실패 (postId=$nextPostId): ${e.message}" }
                publishFailure(nextPostId, "크롤링", e.message)
                return
            }

        if (rawContents.isEmpty()) {
            log.warn { "증시 브리핑 크롤링 결과 없음 (postId=$nextPostId), 포스트Id 업데이트 후 종료" }
            stockBriefingPostStateService.saveLastProcessedPostId(nextPostId)
            return
        }

        val processedContents = mutableListOf<StockBriefingContent>()
        var gptFailureCount = 0

        rawContents.forEach { raw ->
            try {
                val headline =
                    (chatGpt.ask(promptGenerator.toStockBriefingHeadline(raw.title, raw.body)) as? Headline)?.headline
                        ?: raw.title

                val summary =
                    (chatGpt.ask(promptGenerator.toStockBriefingSummary(headline, raw.title, raw.body)) as? Summary)?.summary
                        ?: raw.body

                val highlights =
                    try {
                        (chatGpt.ask(promptGenerator.toKoreanHighlightText(summary)) as? HighlightTexts)?.highlightTexts
                            ?: emptyList()
                    } catch (e: Exception) {
                        log.warn(e) { "하이라이트 텍스트 추출 실패, 빈 리스트 사용: ${raw.title}" }
                        emptyList()
                    }

                processedContents.add(StockBriefingContent(headline, summary, highlights))
                log.info { "GPT 처리 완료: ${raw.title} → $headline" }
            } catch (e: Exception) {
                log.error(e) { "GPT 처리 실패하여 skip 처리 : ${raw.title}" }
                gptFailureCount++
            }
        }

        if (gptFailureCount == rawContents.size) {
            log.error { "증시 브리핑 GPT 처리 전체 실패 (postId=$nextPostId)" }
            publishFailure(nextPostId, "GPT 처리", "모든 컨텐츠($gptFailureCount/${rawContents.size}개) GPT 처리 실패")
            return
        }

        val mainPageBody = generateMainPageBody(processedContents.map { it.headline })

        stockBriefingPostStateService.saveLastProcessedPostId(nextPostId)
        log.info { "증시 브리핑 처리 완료 (postId=$nextPostId): ${processedContents.size}개 (GPT 실패: ${gptFailureCount}개)" }

        applicationEventPublisher.publishEvent(
            StockBriefingContentProcessedEvent(nextPostId, processedContents, mainPageBody = mainPageBody),
        )
    }

    private fun generateMainPageBody(headlines: List<String>): String =
        try {
            (chatGpt.ask(promptGenerator.toStockBriefingMainPageBody(headlines)) as? Summary)?.summary
                ?: headlines.joinToString(" | ")
        } catch (e: Exception) {
            log.warn(e) { "메인 페이지 본문 생성 실패, 헤드라인 조합 사용" }
            headlines.joinToString(" | ")
        }

    private fun publishFailure(
        postId: Long,
        stage: String,
        errorMessage: String?,
    ) {
        applicationEventPublisher.publishEvent(
            StockBriefingInstagramUploadCompletedEvent(
                postId = postId,
                uploadTime = LocalDateTime.now(),
                success = false,
                failedStage = stage,
                errorMessage = errorMessage,
            ),
        )
    }
}
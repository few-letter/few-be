package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.ContentsType
import com.few.common.domain.MediaType
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.core.instagram.StockBriefingContent
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.core.scrapper.naver.StockBriefingRawContent
import com.few.generator.domain.Gen
import com.few.generator.event.StockBriefingContentProcessedEvent
import com.few.generator.event.StockBriefingInstagramUploadCompletedEvent
import com.few.generator.service.GenService
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Component
class StockBriefingSchedulingUseCase(
    private val scrapper: Scrapper,
    private val chatGpt: ChatGpt,
    private val promptGenerator: PromptGenerator,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val genService: GenService,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
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
        val nextPostId =
            scrapper.fetchStockBriefingLatestPostId()
                ?: throw RuntimeException("증시 브리핑 최신 포스트 ID를 가져오지 못했습니다.")
        log.info { "증시 브리핑 최신 포스트 확인 (postId=$nextPostId)" }

        val stockBriefingRawContents: List<StockBriefingRawContent> =
            try {
                scrapper.scrapeStockBriefingPost(nextPostId)
            } catch (e: Exception) {
                log.error(e) { "증시 브리핑 크롤링 실패 (postId=$nextPostId): ${e.message}" }
                publishFailure(nextPostId, "크롤링", e.message)
                return
            }

        if (stockBriefingRawContents.isEmpty()) {
            log.warn { "증시 브리핑 크롤링 결과 없음 (postId=$nextPostId), 종료" }
            return
        }

        val processedContents = mutableListOf<StockBriefingContent>()
        var gptFailureCount = 0

        stockBriefingRawContents.forEach { raw ->
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

                try {
                    genService.saveWithNewTx(
                        Gen(
                            url = null,
                            thumbnailImageUrl = null,
                            mediaType = MediaType.NAVER_STOCK,
                            headline = headline,
                            summary = summary,
                            highlightTexts = gson.toJson(highlights),
                            category = Category.ECONOMY,
                            contentsType = ContentsType.STOCK_BRIEFING,
                        ),
                    )
                    log.info { "증시 브리핑 Gen 저장 완료: $headline" }
                } catch (e: Exception) {
                    log.error(e) { "증시 브리핑 Gen 저장 실패하여 skip 처리 : ${raw.title}" }
                }
            } catch (e: Exception) {
                log.error(e) { "GPT 처리 실패하여 skip 처리 : ${raw.title}" }
                gptFailureCount++
            }
        }

        if (gptFailureCount == stockBriefingRawContents.size) {
            log.error { "증시 브리핑 GPT 처리 전체 실패 (postId=$nextPostId)" }
            publishFailure(nextPostId, "GPT 처리", "모든 컨텐츠($gptFailureCount/${stockBriefingRawContents.size}개) GPT 처리 실패")
            return
        }

        val mainPageBody = generateMainPageBody(processedContents.map { it.headline })

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
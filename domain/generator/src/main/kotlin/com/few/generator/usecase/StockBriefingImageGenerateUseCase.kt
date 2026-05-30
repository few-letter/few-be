package com.few.generator.usecase

import com.few.generator.core.instagram.MainPageCardGenerator
import com.few.generator.core.instagram.NewsContent
import com.few.generator.core.instagram.SingleNewsCardGenerator
import com.few.generator.event.StockBriefingContentProcessedEvent
import com.few.generator.event.StockBriefingImageGeneratedEvent
import com.few.generator.event.StockBriefingInstagramUploadCompletedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class StockBriefingImageGenerateUseCase(
    private val singleNewsCardGenerator: SingleNewsCardGenerator,
    private val mainPageCardGenerator: MainPageCardGenerator,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val BRIEFING_CATEGORY = "briefing"
        private const val MAX_CAROUSEL_IMAGES = 9
    }

    @Async("generatorSchedulingExecutor")
    @EventListener
    fun onStockBriefingContentProcessed(event: StockBriefingContentProcessedEvent) {
        log.info { "증시 브리핑 콘텐츠 처리 완료 감지 (postId=${event.postId}), 카드뉴스 이미지 생성 시작" }

        val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val detailImagePaths = mutableListOf<String>()

        try {
            val contentsToProcess = event.contents.take(MAX_CAROUSEL_IMAGES)

            contentsToProcess.forEachIndexed { index, content ->
                try {
                    val fileName = "gen_images/${dateStr}_briefing_${event.postId}_${index + 1}.png"
                    val newsContent =
                        NewsContent(
                            headline = content.headline,
                            summary = content.summary,
                            category = BRIEFING_CATEGORY,
                            createdAt = LocalDateTime.now(),
                            highlightTexts = content.highlightTexts,
                        )
                    if (singleNewsCardGenerator.generateImage(newsContent, fileName)) {
                        detailImagePaths.add(fileName)
                        log.info { "[${index + 1}/${contentsToProcess.size}] 상세 카드 이미지 생성 완료: $fileName" }
                    } else {
                        log.error { "[${index + 1}/${contentsToProcess.size}] 상세 카드 이미지 생성 실패: ${content.headline}" }
                    }
                } catch (e: Exception) {
                    log.error(e) { "[${index + 1}/${contentsToProcess.size}] 상세 카드 이미지 생성 중 예외: ${content.headline}" }
                }
            }

            if (detailImagePaths.isEmpty()) {
                log.error { "증시 브리핑 상세 카드 이미지 생성 전체 실패 (postId=${event.postId})" }
                publishFailure(event.postId, "이미지 생성", "상세 카드 이미지 생성 전체 실패")
                return
            }

            val mainPagePath = "gen_images/${dateStr}_briefing_${event.postId}_main.png"

            if (!mainPageCardGenerator.generateBriefingMainPageImage(event.mainPageBody, mainPagePath)) {
                log.error { "증시 브리핑 표지 이미지 생성 실패 (postId=${event.postId})" }
                publishFailure(event.postId, "표지 이미지 생성", "표지 이미지 생성 실패")
                return
            }

            log.info { "증시 브리핑 표지 이미지 생성 완료: $mainPagePath" }

            applicationEventPublisher.publishEvent(
                StockBriefingImageGeneratedEvent(
                    postId = event.postId,
                    detailImagePaths = detailImagePaths,
                    mainPageImagePath = mainPagePath,
                    headlines = event.headlines,
                ),
            )
            log.info { "증시 브리핑 이미지 생성 완료 이벤트 발행 (postId=${event.postId}): 상세 ${detailImagePaths.size}개, 표지 1개" }
        } catch (e: Exception) {
            log.error(e) { "증시 브리핑 이미지 생성 중 예외 발생 (postId=${event.postId}): ${e.message}" }
            publishFailure(event.postId, "이미지 생성", e.message)
        }
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
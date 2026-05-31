package com.few.generator.usecase

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Keywords
import com.few.generator.core.instagram.InstagramUploader
import com.few.generator.event.StockBriefingInstagramUploadCompletedEvent
import com.few.generator.event.StockBriefingS3UploadedEvent
import com.few.generator.support.utils.DelayUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Component
class UploadStockBriefingInstagramUseCase(
    private val instagramUploader: InstagramUploader,
    private val chatGpt: ChatGpt,
    private val promptGenerator: PromptGenerator,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @Qualifier("instagramCoroutineScope")
    private val scope: CoroutineScope,
) {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val MAX_HASHTAGS = 5
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
        private val FALLBACK_HASHTAGS = listOf("증시브리핑", "주식", "코스피", "나스닥", "주식투자")
    }

    @EventListener
    fun onStockBriefingS3Uploaded(event: StockBriefingS3UploadedEvent) {
        if (event.detailImageUrls.isEmpty()) {
            log.warn { "증시 브리핑 S3 업로드된 이미지 없음 (postId=${event.postId}), Instagram 업로드 건너뜀" }
            publishFailure(event.postId, event.uploadTime, "S3 업로드", "업로드된 이미지 없음")
            return
        }

        log.info { "증시 브리핑 S3 업로드 완료 감지 (postId=${event.postId}), Instagram 업로드 시작" }
        scope.launch { performUpload(event) }
    }

    private suspend fun performUpload(event: StockBriefingS3UploadedEvent) {
        try {
            val allImageUrls =
                if (event.mainPageImageUrl != null) {
                    listOf(event.mainPageImageUrl) + event.detailImageUrls
                } else {
                    event.detailImageUrls
                }

            val caption = generateCaption(event.uploadTime, event.headlines)

            val childCreationIds = mutableListOf<String>()
            allImageUrls.forEachIndexed { index, imageUrl ->
                val childId = instagramUploader.createChildMediaContainer(imageUrl)
                if (childId != null) {
                    childCreationIds.add(childId)
                    log.info { "Child 컨테이너 생성 성공 [${index + 1}/${allImageUrls.size}]: $childId" }
                } else {
                    log.error { "Child 컨테이너 생성 실패 [${index + 1}/${allImageUrls.size}]: $imageUrl" }
                }
            }

            if (childCreationIds.isEmpty()) {
                log.error { "증시 브리핑 Child 컨테이너 생성 전체 실패 (postId=${event.postId})" }
                publishFailure(event.postId, event.uploadTime, "인스타그램 업로드", "Child 컨테이너 생성 실패")
                return
            }

            val parentCreationId = instagramUploader.createParentMediaContainer(childCreationIds, caption)
            if (parentCreationId == null) {
                log.error { "증시 브리핑 Parent 컨테이너 생성 실패 (postId=${event.postId})" }
                publishFailure(event.postId, event.uploadTime, "인스타그램 업로드", "Parent 컨테이너 생성 실패")
                return
            }

            DelayUtil.randomDelay(600, 1801)

            val publishSuccess = instagramUploader.publishMedia(parentCreationId)
            if (!publishSuccess) {
                log.error { "증시 브리핑 Instagram 게시 실패 (postId=${event.postId})" }
                publishFailure(event.postId, event.uploadTime, "인스타그램 업로드", "게시물 게시 실패")
                return
            }

            log.info { "증시 브리핑 Instagram carousel 게시 성공 (postId=${event.postId}): ${allImageUrls.size}개 이미지" }
            applicationEventPublisher.publishEvent(
                StockBriefingInstagramUploadCompletedEvent(
                    postId = event.postId,
                    uploadTime = event.uploadTime,
                    success = true,
                ),
            )
        } catch (e: Exception) {
            log.error(e) { "증시 브리핑 Instagram 업로드 중 예외 발생 (postId=${event.postId}): ${e.message}" }
            val errorMsg = e.message ?: ""
            if (errorMsg.contains("Application request limit reached")) {
                DelayUtil.randomDelay(3600, 4000)
            } else {
                DelayUtil.randomDelay(600, 700)
            }
            publishFailure(event.postId, event.uploadTime, "인스타그램 업로드", e.message)
        }
    }

    fun generateCaption(
        uploadTime: LocalDateTime,
        headlines: List<String>,
    ): String {
        val hashtags = generateDynamicHashtags(headlines)
        val allHashtags = hashtags.joinToString(" ") { "#$it" }

        return buildString {
            appendLine("📈 few letter가 정리한 ${uploadTime.format(DATE_FORMATTER)} 증시 브리핑")
            appendLine()
            if (allHashtags.isNotEmpty()) {
                append(allHashtags)
            }
        }
    }

    fun generateDynamicHashtags(headlines: List<String>): List<String> {
        if (headlines.isEmpty()) return FALLBACK_HASHTAGS

        return try {
            val prompt = promptGenerator.toInstagramHashtags(headlines, MAX_HASHTAGS)
            val keywords = chatGpt.ask(prompt) as? Keywords ?: return FALLBACK_HASHTAGS
            keywords.keywords.map { it.replace(" ", "") }.take(MAX_HASHTAGS)
        } catch (e: Exception) {
            log.warn(e) { "증시 브리핑 동적 해시태그 생성 실패, 기본 해시태그 사용" }
            FALLBACK_HASHTAGS
        }
    }

    private fun publishFailure(
        postId: Long,
        uploadTime: LocalDateTime,
        stage: String,
        errorMessage: String?,
    ) {
        applicationEventPublisher.publishEvent(
            StockBriefingInstagramUploadCompletedEvent(
                postId = postId,
                uploadTime = uploadTime,
                success = false,
                failedStage = stage,
                errorMessage = errorMessage,
            ),
        )
    }
}
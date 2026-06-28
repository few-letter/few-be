package com.few.generator.usecase

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Keywords
import com.few.generator.core.instagram.InstagramUploader
import com.few.generator.event.PopularNasdaqCardNewsS3UploadedEvent
import com.few.generator.support.utils.DelayUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Component
class PopularNasdaqCardNewsInstagramUploadUseCase(
    private val instagramUploader: InstagramUploader,
    private val chatGpt: ChatGpt,
    private val promptGenerator: PromptGenerator,
    @Value("\${generator.instagram.card-news-upload-enabled}")
    private val cardNewsUploadEnabled: Boolean,
    @Value("\${generator.instagram.popular-nasdaq-max-images}")
    private val popularNasdaqMaxImages: Int,
    @Qualifier("instagramCoroutineScope")
    private val scope: CoroutineScope,
) {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val MAX_HASHTAGS = 5
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
        private val FALLBACK_HASHTAGS = listOf("나스닥", "미국주식", "주식", "주식투자", "NASDAQ")
    }

    @EventListener
    fun onPopularNasdaqCardNewsS3Uploaded(event: PopularNasdaqCardNewsS3UploadedEvent) {
        if (!cardNewsUploadEnabled) {
            log.info { "Popular Nasdaq 인스타그램 업로드가 비활성화되어 Skip 합니다." }
            return
        }
        if (event.detailImageUrlsByStock.isEmpty()) {
            log.warn { "Popular Nasdaq S3 업로드된 이미지가 없어 Instagram 업로드를 건너뜁니다." }
            return
        }

        log.info { "Popular Nasdaq S3 업로드 완료 감지, Instagram 업로드 시작 (${event.detailImageUrlsByStock.size}개 종목)" }
        scope.launch { performUpload(event) }
    }

    private suspend fun performUpload(event: PopularNasdaqCardNewsS3UploadedEvent) {
        event.detailImageUrlsByStock.forEach { (stockName, detailUrls) ->
            try {
                val mainPageUrl = event.mainPageImageUrlsByStock[stockName]
                val allImageUrls =
                    (if (mainPageUrl != null) listOf(mainPageUrl) + detailUrls else detailUrls)
                        .take(popularNasdaqMaxImages)
                val headlines = event.headlinesByStock[stockName] ?: throw IllegalStateException("[$stockName] 헤드라인 정보 없음")

                val caption = generateCaption(stockName, event.uploadTime, headlines)
                uploadCarousel(stockName, allImageUrls, caption)
            } catch (e: Exception) {
                log.error(e) { "[$stockName] Instagram 업로드 처리 중 예외 발생: ${e.message}" }
            }
        }
    }

    fun generateCaption(
        stockName: String,
        uploadTime: LocalDateTime,
        headlines: List<String>,
    ): String {
        val hashtags = generateDynamicHashtags(headlines)
        val allHashtags = hashtags.joinToString(" ") { "#$it" }

        return buildString {
            appendLine("📈 few letter가 정리한 ${uploadTime.format(DATE_FORMATTER)} $stockName 주요소식")
            appendLine()
            headlines.forEach { appendLine(it) }
            if (allHashtags.isNotEmpty()) {
                appendLine()
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
            log.warn(e) { "Popular Nasdaq 동적 해시태그 생성 실패, 기본 해시태그 사용" }
            FALLBACK_HASHTAGS
        }
    }

    private suspend fun uploadCarousel(
        stockName: String,
        imageUrls: List<String>,
        caption: String,
    ) {
        log.info { "[$stockName] Instagram carousel 업로드 시작: ${imageUrls.size}개 이미지" }

        try {
            val childCreationIds = mutableListOf<String>()
            imageUrls.forEachIndexed { index, imageUrl ->
                val childId = instagramUploader.createChildMediaContainer(imageUrl)
                if (childId != null) {
                    childCreationIds.add(childId)
                    log.info { "[$stockName] Child 컨테이너 생성 성공 [${index + 1}/${imageUrls.size}]: $childId" }
                } else {
                    log.error { "[$stockName] Child 컨테이너 생성 실패 [${index + 1}/${imageUrls.size}]: $imageUrl" }
                }
            }

            if (childCreationIds.isEmpty()) {
                log.error { "[$stockName] 생성된 Child 컨테이너가 없어 carousel 업로드를 건너뜁니다." }
                return
            }

            val parentCreationId = instagramUploader.createParentMediaContainer(childCreationIds, caption)
            if (parentCreationId == null) {
                log.error { "[$stockName] Parent 컨테이너 생성 실패" }
                return
            }
            log.info { "[$stockName] Parent 컨테이너 생성 성공: $parentCreationId" }

            DelayUtil.randomDelay(600, 1801)

            val publishSuccess = instagramUploader.publishMedia(parentCreationId)
            if (publishSuccess) {
                log.info { "[$stockName] Instagram carousel 게시 성공: ${imageUrls.size}개 이미지" }
            } else {
                log.error { "[$stockName] Instagram carousel 게시 실패: parentCreationId=$parentCreationId" }
            }
        } catch (e: Exception) {
            log.error(e) { "[$stockName] Instagram carousel 업로드 중 오류 발생: ${e.message}" }

            val errorMsg = e.message ?: ""
            if (errorMsg.contains("Application request limit reached")) {
                log.info { "[$stockName] API 제한 도달, 약 1시간 대기" }
                DelayUtil.randomDelay(3600, 4000)
            } else {
                DelayUtil.randomDelay(600, 700)
            }
        }
    }
}
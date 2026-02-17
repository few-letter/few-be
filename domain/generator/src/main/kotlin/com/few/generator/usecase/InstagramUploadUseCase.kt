package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Keywords
import com.few.generator.core.instagram.InstagramUploader
import com.few.generator.event.CardNewsS3UploadedEvent
import com.few.generator.event.InstagramUploadCompletedEvent
import com.few.generator.service.GenService
import com.few.generator.support.utils.DelayUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Random

@Component
class InstagramUploadUseCase(
    private val instagramUploader: InstagramUploader,
    private val genService: GenService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val chatGpt: ChatGpt,
    private val promptGenerator: PromptGenerator,
    @Value("\${generator.contents.countByCategory}")
    protected val contentsCountByCategory: Int,
) {
    private val log = KotlinLogging.logger {}
    private val random = Random()

    companion object {
        private const val MAX_HASHTAGS_PER_CATEGORY = 5
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)

        // 카테고리별 이모티콘
        private val CATEGORY_EMOJI_MAP =
            mapOf(
                Category.TECHNOLOGY to "🔬",
                Category.POLITICS to "🏛️",
                Category.ECONOMY to "💰",
                Category.SOCIETY to "🌍",
                Category.LIFE to "🏠",
            )
    }

    @Async("generatorSchedulingExecutor")
    @EventListener
    fun onCardNewsS3Uploaded(event: CardNewsS3UploadedEvent) {
        if (event.uploadedUrlsByCategory.isEmpty()) {
            log.warn { "${event.region.name} S3 업로드된 이미지가 없어 Instagram 업로드를 건너뜁니다." }
            return
        }

        log.info { "${event.region.name} S3 업로드 완료 감지, Instagram 업로드 시작 (${event.uploadedUrlsByCategory.size}개 카테고리)" }

        val successCategories = mutableListOf<Category>()
        val failedCategories = mutableListOf<Category>()
        val errorMessages = mutableMapOf<Category, String>()

        try {
            // 카테고리별로 carousel 업로드 수행
            event.uploadedUrlsByCategory.forEach { (category, imageUrls) ->
                try {
                    // 표지 이미지를 가장 앞에 추가
                    val mainPageUrl = event.mainPageUrlsByCategory[category]
                    val allImageUrls =
                        if (mainPageUrl != null) {
                            listOf(mainPageUrl) + imageUrls
                        } else {
                            imageUrls
                        }
                    val hasMainPage = mainPageUrl != null

                    val caption = generateCaption(category, event.region, event.uploadTime)
                    val result = uploadCarouselByCategory(category, allImageUrls, caption, hasMainPage)
                    if (result.success) {
                        successCategories.add(category)
                    } else {
                        failedCategories.add(category)
                        result.errorMessage?.let { errorMessages[category] = it }
                    }
                } catch (e: Exception) {
                    log.error(e) { "[${category.title}] Instagram 업로드 처리 중 예외 발생: ${e.message}" }
                    failedCategories.add(category)
                    errorMessages[category] = e.message ?: "알 수 없는 오류"
                }
            }
        } catch (e: Exception) {
            log.error(e) { "${event.region.name} Instagram 업로드 중 예외 발생: ${e.message}" }
        } finally {
            // 예외 발생 여부와 관계없이 이벤트 발행
            applicationEventPublisher.publishEvent(
                InstagramUploadCompletedEvent(
                    region = event.region,
                    uploadTime = event.uploadTime,
                    successCategories = successCategories,
                    failedCategories = failedCategories,
                    errorMessages = errorMessages,
                ),
            )
            log.info { "${event.region.name} Instagram 업로드 완료 이벤트 발행: 성공 ${successCategories.size}개, 실패 ${failedCategories.size}개" }
        }
    }

    fun generateCaption(
        category: Category,
        region: Region,
        uploadTime: LocalDateTime,
    ): String {
        val gens = genService.findAllByCreatedAtTodayAndCategoryAndRegion(category, region)
        val emoji = CATEGORY_EMOJI_MAP[category] ?: "📰"
        val dynamicHashtags = generateDynamicHashtags(gens.map { it.headline })
        val allHashtags = dynamicHashtags.joinToString(" ") { "#$it" }

        return buildString {
            // 첫 줄: 제목
            appendLine("few letter가 정리한 ${uploadTime.format(DATE_FORMATTER)}의 ${category.title} 뉴스 ${gens.size}개")
            appendLine()

            // 각 gen별 headline 추가
            gens.forEach { gen ->
                appendLine("$emoji ${gen.headline}")
            }

            // 해시태그 추가 (동적)
            if (allHashtags.isNotEmpty()) {
                appendLine()
                append(allHashtags)
            }
        }
    }

    private fun generateDynamicHashtags(headlines: List<String>): List<String> {
        if (headlines.isEmpty() || MAX_HASHTAGS_PER_CATEGORY <= 0) return emptyList()

        return try {
            val prompt = promptGenerator.toInstagramHashtags(headlines, MAX_HASHTAGS_PER_CATEGORY)
            val keywords =
                chatGpt.ask(prompt) as? Keywords
                    ?: throw IllegalStateException("ChatGPT 응답을 Keywords로 변환할 수 없습니다")
            /**
             * 해시테그 내 공백 대체
             * ex. "삼성 전자" -> "삼성전자"
             */
            keywords.keywords.map { it.replace(" ", "") }.take(MAX_HASHTAGS_PER_CATEGORY)
        } catch (e: Exception) {
            log.warn(e) { "GPT를 통한 동적 해시태그 생성 실패, 빈 해시태그로 대체합니다: ${e.message}" }
            emptyList()
        }
    }

    private fun uploadCarouselByCategory(
        category: Category,
        imageUrls: List<String>,
        caption: String,
        hasMainPage: Boolean = false,
    ): UploadResult {
        log.info { "[${category.title}] Instagram carousel 업로드 시작: ${imageUrls.size}개 이미지 (표지 포함: $hasMainPage)" }

        try {
            // 0단계: 조건 체크 (표지 이미지는 콘텐츠 개수에서 제외)
            val contentImageCount = if (hasMainPage) imageUrls.size - 1 else imageUrls.size
            if (contentImageCount !in 2..contentsCountByCategory) {
                return UploadResult(
                    success = false,
                    errorMessage = "${category.title} 카테고리 이미지 개수 유효하지 않음: $contentImageCount",
                )
            }

            // 1단계: 각 이미지에 대해 Child Media Container 생성
            val childCreationIds = mutableListOf<String>()
            imageUrls.forEachIndexed { index, imageUrl ->
                val childCreationId = instagramUploader.createChildMediaContainer(imageUrl)
                if (childCreationId != null) {
                    childCreationIds.add(childCreationId)
                    log.info { "[${category.title}] Child 컨테이너 생성 성공 [${index + 1}/${imageUrls.size}]: creationId=$childCreationId" }
                } else {
                    log.error { "[${category.title}] Child 컨테이너 생성 실패 [${index + 1}/${imageUrls.size}]: $imageUrl" }
                }
            }

            if (childCreationIds.isEmpty()) {
                log.error { "[${category.title}] 생성된 Child 컨테이너가 없어 carousel 업로드를 건너뜁니다." }
                return UploadResult(success = false, errorMessage = "Child 컨테이너 생성 실패")
            }

            // 2단계: Parent Media Container 생성 (Carousel)
            val parentCreationId = instagramUploader.createParentMediaContainer(childCreationIds, caption)
            if (parentCreationId == null) {
                log.error { "[${category.title}] Parent 컨테이너 생성 실패" }
                return UploadResult(success = false, errorMessage = "Parent 컨테이너 생성 실패")
            }
            log.info { "[${category.title}] Parent 컨테이너 생성 성공: creationId=$parentCreationId" }

            DelayUtil.randomDelay(600, 1801)

            // 3단계: 게시물 게시
            val publishSuccess = instagramUploader.publishMedia(parentCreationId)
            return if (publishSuccess) {
                log.info { "[${category.title}] Instagram carousel 게시 성공: ${imageUrls.size}개 이미지" }
                UploadResult(success = true)
            } else {
                log.error { "[${category.title}] Instagram carousel 게시 실패: parentCreationId=$parentCreationId" }
                UploadResult(success = false, errorMessage = "게시물 게시 실패")
            }
        } catch (e: Exception) {
            log.error(e) { "[${category.title}] Instagram carousel 업로드 중 오류 발생: ${e.message}" }

            val errorMsg = e.message ?: ""

            if (errorMsg.contains("Application request limit reached")) {
                log.info { "⚠️ [한도 초과] API 제한에 도달했습니다. 약 1시간 동안 대기합니다..." }
                DelayUtil.randomDelay(3600, 4000)
            } else {
                log.info { "❌ [에러] 예상치 못한 오류 발생: $errorMsg" }
                DelayUtil.randomDelay(600, 700)
            }

            return UploadResult(success = false, errorMessage = e.message)
        }
    }

    private data class UploadResult(
        val success: Boolean,
        val errorMessage: String? = null,
    )
}
package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.generator.core.instagram.InstagramUploader
import com.few.generator.event.CardNewsS3UploadedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter
import java.util.Locale

@Component
class InstagramUploadUseCase(
    private val instagramUploader: InstagramUploader,
) {
    private val log = KotlinLogging.logger {}

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
    }

    @Async("generatorSchedulingExecutor")
    @EventListener
    fun onCardNewsS3Uploaded(event: CardNewsS3UploadedEvent) {
        if (event.uploadedUrlsByCategory.isEmpty()) {
            log.warn { "${event.region.name} S3 업로드된 이미지가 없어 Instagram 업로드를 건너뜁니다." }
            return
        }

        log.info { "${event.region.name} S3 업로드 완료 감지, Instagram 업로드 시작 (${event.uploadedUrlsByCategory.size}개 카테고리)" }

        // 카테고리별로 carousel 업로드 수행
        event.uploadedUrlsByCategory.forEach { (category, imageUrls) ->
            val caption = "${event.uploadTime.format(DATE_FORMATTER)} 주요 ${category.title} 뉴스"
            uploadCarouselByCategory(category, imageUrls, caption)
        }
    }

    private fun uploadCarouselByCategory(
        category: Category,
        imageUrls: List<String>,
        caption: String,
    ) {
        if (imageUrls.isEmpty()) {
            log.warn { "[${category.title}] 업로드할 이미지가 없습니다." }
            return
        }

        log.info { "[${category.title}] Instagram carousel 업로드 시작: ${imageUrls.size}개 이미지" }

        try {
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
                return
            }

            // 2단계: Parent Media Container 생성 (Carousel)
            val parentCreationId = instagramUploader.createParentMediaContainer(childCreationIds, caption)
            if (parentCreationId == null) {
                log.error { "[${category.title}] Parent 컨테이너 생성 실패" }
                return
            }
            log.info { "[${category.title}] Parent 컨테이너 생성 성공: creationId=$parentCreationId" }

            // 3단계: 게시물 게시
            val publishSuccess = instagramUploader.publishMedia(parentCreationId)
            if (publishSuccess) {
                log.info { "[${category.title}] Instagram carousel 게시 성공: ${imageUrls.size}개 이미지" }
            } else {
                log.error { "[${category.title}] Instagram carousel 게시 실패: parentCreationId=$parentCreationId" }
            }
        } catch (e: Exception) {
            log.error(e) { "[${category.title}] Instagram carousel 업로드 중 오류 발생: ${e.message}" }
        }
    }
}
package com.few.generator.usecase

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
        if (event.uploadedUrls.isEmpty()) {
            log.warn { "${event.region.name} S3 업로드된 이미지가 없어 Instagram 업로드를 건너뜁니다." }
            return
        }

        log.info { "${event.region.name} S3 업로드 완료 감지, Instagram 업로드 시작" }

        // 첫 번째 이미지만 업로드 (추후 확장 예정)
        val imageUrl = event.uploadedUrls.first()
        val caption = "${event.uploadTime.format(DATE_FORMATTER)} 주요 뉴스"

        try {
            // 1단계: 미디어 컨테이너 생성
            val creationId = instagramUploader.createChildMediaContainer(imageUrl, caption)
            if (creationId == null) {
                log.error { "Instagram 미디어 컨테이너 생성 실패: $imageUrl" }
                return
            }
            log.info { "Instagram 미디어 컨테이너 생성 성공: creationId=$creationId" }

            // 2단계: 게시물 게시
            val publishSuccess = instagramUploader.publishMedia(creationId)
            if (publishSuccess) {
                log.info { "Instagram 게시물 게시 성공: $imageUrl" }
            } else {
                log.error { "Instagram 게시물 게시 실패: creationId=$creationId" }
            }
        } catch (e: Exception) {
            log.error(e) { "Instagram 업로드 중 오류 발생: ${e.message}" }
        }
    }
}
package com.few.generator.usecase

import com.few.generator.event.CardNewsImageGeneratedEvent
import com.few.generator.event.CardNewsS3UploadedEvent
import com.few.generator.support.aws.S3Provider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime

@Component
class UploadGenCardNewsS3UseCase(
    private val s3Provider: S3Provider,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val log = KotlinLogging.logger {}

    @Async("generatorSchedulingExecutor")
    @EventListener
    fun onCardNewsImageGenerated(event: CardNewsImageGeneratedEvent) {
        log.info { "${event.region.name} 카드뉴스 이미지 생성 완료 감지, S3 업로드 시작" }

        val uploadTime = LocalDateTime.now()
        val totalCount = event.imagePaths.size
        val (uploadedCount, errorMessage) = uploadImagesToS3(event.imagePaths)

        removeImageFiles(event.imagePaths)

        log.info { "${event.region.name} 카드뉴스 S3 업로드 완료: $uploadedCount / ${totalCount}개 성공" }

        // S3 업로드 완료 이벤트 발행
        applicationEventPublisher.publishEvent(
            CardNewsS3UploadedEvent(
                region = event.region,
                uploadedCount = uploadedCount,
                totalCount = totalCount,
                uploadTime = uploadTime,
                errorMessage = errorMessage,
            ),
        )
        log.info { "${event.region.name} 카드뉴스 S3 업로드 완료 이벤트 발행: $uploadedCount / ${totalCount}개" }
    }

    private fun removeImageFiles(imagePaths: List<String>) {
        imagePaths.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                if (file.delete()) {
                    log.debug { "로컬 파일 삭제 성공: $path" }
                } else {
                    log.warn { "로컬 파일 삭제 실패: $path" }
                }
            }
        }
    }

    private fun uploadImagesToS3(imagePaths: List<String>): Pair<Int, String?> {
        // S3에 업로드 (파일별 개별 처리)
        val result = s3Provider.uploadImages(imagePaths)

        log.info {
            "S3 업로드 완료: ${result.uploadedCount}개 성공, ${result.failedCount}개 실패 (총 ${result.totalCount}개)"
        }

        // 실패한 업로드가 있으면 로그 출력
        if (result.failedCount > 0) {
            log.warn { "업로드 실패 파일 목록:\n${result.getErrorMessage()}" }
        }

        return Pair(result.uploadedCount, result.getErrorMessage())
    }
}
package com.few.generator.usecase

import com.few.generator.event.StockBriefingImageGeneratedEvent
import com.few.generator.event.StockBriefingInstagramUploadCompletedEvent
import com.few.generator.event.StockBriefingS3UploadedEvent
import com.few.generator.support.aws.S3Provider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime

@Component
class UploadStockBriefingS3UseCase(
    private val s3Provider: S3Provider,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val log = KotlinLogging.logger {}

    @Async("generatorSchedulingExecutor")
    @EventListener
    fun onStockBriefingImageGenerated(event: StockBriefingImageGeneratedEvent) {
        log.info { "증시 브리핑 이미지 생성 완료 감지 (postId=${event.postId}), S3 업로드 시작" }

        val uploadTime = LocalDateTime.now()
        val allPaths = event.detailImagePaths + listOfNotNull(event.mainPageImagePath)

        try {
            val detailUploadResult = s3Provider.uploadImages(event.detailImagePaths)
            val detailUrls = detailUploadResult.successfulUploads.map { it.url }
            log.info { "증시 브리핑 상세 이미지 S3 업로드: ${detailUploadResult.uploadedCount}/${detailUploadResult.totalCount}개 성공" }

            if (detailUrls.isEmpty()) {
                log.error { "증시 브리핑 상세 이미지 S3 업로드 전체 실패 (postId=${event.postId})" }
                removeImageFiles(allPaths)
                publishFailure(event.postId, "S3 업로드", detailUploadResult.getErrorMessage())
                return
            }

            var mainPageUrl: String? = null
            if (event.mainPageImagePath != null) {
                val mainUploadResult = s3Provider.uploadImages(listOf(event.mainPageImagePath))
                if (mainUploadResult.successfulUploads.isNotEmpty()) {
                    mainPageUrl = mainUploadResult.successfulUploads.first().url
                    log.info { "증시 브리핑 표지 이미지 S3 업로드 성공" }
                } else {
                    log.warn { "증시 브리핑 표지 이미지 S3 업로드 실패: ${mainUploadResult.getErrorMessage()}" }
                }
            }

            removeImageFiles(allPaths)

            applicationEventPublisher.publishEvent(
                StockBriefingS3UploadedEvent(
                    postId = event.postId,
                    uploadTime = uploadTime,
                    detailImageUrls = detailUrls,
                    mainPageImageUrl = mainPageUrl,
                    headlines = event.headlines,
                ),
            )
            log.info {
                "증시 브리핑 S3 업로드 완료 이벤트 발행 (postId=${event.postId}): 상세 ${detailUrls.size}개, 표지 ${if (mainPageUrl != null) "1개" else "없음"}"
            }
        } catch (e: Exception) {
            log.error(e) { "증시 브리핑 S3 업로드 중 예외 발생 (postId=${event.postId}): ${e.message}" }
            removeImageFiles(allPaths)
            publishFailure(event.postId, "S3 업로드", e.message)
        }
    }

    private fun removeImageFiles(paths: List<String>) {
        paths.forEach { path ->
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
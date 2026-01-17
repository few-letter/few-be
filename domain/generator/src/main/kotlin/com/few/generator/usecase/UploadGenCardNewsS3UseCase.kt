package com.few.generator.usecase

import com.few.common.domain.Category
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
        val totalCount = event.imagePathsByCategory.values.sumOf { it.size }
        var uploadedCount = 0
        var uploadedUrlsByCategory: Map<Category, List<String>> = emptyMap()
        var errorMessage: String? = null

        try {
            val result = uploadImagesToS3ByCategory(event.imagePathsByCategory)
            uploadedCount = result.first
            uploadedUrlsByCategory = result.second
            errorMessage = result.third

            log.info { "${event.region.name} 카드뉴스 S3 업로드 완료: $uploadedCount / ${totalCount}개 성공 (${uploadedUrlsByCategory.size}개 카테고리)" }
        } catch (e: Exception) {
            log.error(e) { "${event.region.name} 카드뉴스 S3 업로드 중 예외 발생: ${e.message}" }
            errorMessage = e.message ?: "알 수 없는 오류"
        } finally {
            // 모든 이미지 파일 삭제
            val allImagePaths = event.imagePathsByCategory.values.flatten()
            removeImageFiles(allImagePaths)

            // 예외 발생 여부와 관계없이 S3 업로드 완료 이벤트 발행
            applicationEventPublisher.publishEvent(
                CardNewsS3UploadedEvent(
                    region = event.region,
                    uploadedCount = uploadedCount,
                    totalCount = totalCount,
                    uploadTime = uploadTime,
                    uploadedUrlsByCategory = uploadedUrlsByCategory,
                    errorMessage = errorMessage,
                ),
            )
            log.info {
                "${event.region.name} 카드뉴스 S3 업로드 완료 이벤트 발행: $uploadedCount / ${totalCount}개 (${uploadedUrlsByCategory.size}개 카테고리)"
            }
        }
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

    private fun uploadImagesToS3ByCategory(
        imagePathsByCategory: Map<Category, List<String>>,
    ): Triple<Int, Map<Category, List<String>>, String?> {
        val uploadedUrlsByCategory = mutableMapOf<Category, List<String>>()
        val errorMessages = mutableListOf<String>()
        var totalUploadedCount = 0

        imagePathsByCategory.forEach { (category, imagePaths) ->
            val result = s3Provider.uploadImages(imagePaths)

            log.info {
                "[${category.title}] S3 업로드 완료: ${result.uploadedCount}개 성공, ${result.failedCount}개 실패 (총 ${result.totalCount}개)"
            }

            if (result.successfulUploads.isNotEmpty()) {
                uploadedUrlsByCategory[category] = result.successfulUploads.map { it.url }
            }

            totalUploadedCount += result.uploadedCount

            if (result.failedCount > 0) {
                log.warn { "[${category.title}] 업로드 실패 파일 목록:\n${result.getErrorMessage()}" }
                result.getErrorMessage()?.let { errorMessages.add("[${category.title}] $it") }
            }
        }

        val combinedErrorMessage = if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null
        return Triple(totalUploadedCount, uploadedUrlsByCategory, combinedErrorMessage)
    }
}
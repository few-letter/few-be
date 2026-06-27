package com.few.generator.usecase

import com.few.generator.event.PopularNasdaqCardNewsImageGeneratedEvent
import com.few.generator.support.aws.S3Provider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.File

@Component
class UploadPopularNasdaqCardNewsS3UseCase(
    private val s3Provider: S3Provider,
) {
    private val log = KotlinLogging.logger {}

    @Async("generatorSchedulingExecutor")
    @EventListener
    fun onPopularNasdaqCardNewsImageGenerated(event: PopularNasdaqCardNewsImageGeneratedEvent) {
        log.info { "Popular Nasdaq 카드뉴스 이미지 생성 완료 감지, S3 업로드 시작" }

        val allDetailPaths = event.imagePathsByStock.values.flatten()
        val allMainPaths = event.mainPageImagePathsByStock.values.toList()
        val allPaths = allDetailPaths + allMainPaths

        try {
            val result = s3Provider.uploadImages(allPaths)
            log.info { "Popular Nasdaq 카드뉴스 S3 업로드 완료: ${result.uploadedCount}/${result.totalCount}개 성공" }
            if (result.failedCount > 0) {
                log.warn { "Popular Nasdaq S3 업로드 실패:\n${result.getErrorMessage()}" }
            }
        } catch (e: Exception) {
            log.error(e) { "Popular Nasdaq 카드뉴스 S3 업로드 중 예외 발생: ${e.message}" }
        } finally {
            removeImageFiles(allPaths)
        }
    }

    private fun removeImageFiles(imagePaths: List<String>) {
        imagePaths.forEach { path ->
            val file = File(path)
            if (file.exists() && file.delete()) {
                log.debug { "로컬 파일 삭제 성공: $path" }
            }
        }
    }
}
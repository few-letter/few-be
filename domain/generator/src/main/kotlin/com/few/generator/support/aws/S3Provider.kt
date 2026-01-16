package com.few.generator.support.aws

import io.awspring.cloud.s3.S3Template
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File

data class S3UploadResult(
    val successfulUploads: List<SuccessfulUpload>,
    val failedUploads: List<FailedUpload>,
) {
    val uploadedCount: Int get() = successfulUploads.size
    val failedCount: Int get() = failedUploads.size
    val totalCount: Int get() = uploadedCount + failedCount

    fun getErrorMessage(): String? {
        if (failedUploads.isEmpty()) return null
        return buildString {
            appendLine("Failed to upload ${failedUploads.size} file(s):")
            failedUploads.forEachIndexed { index, failed ->
                appendLine("${index + 1}. ${failed.path}: ${failed.errorMessage}")
            }
        }.trim()
    }
}

data class SuccessfulUpload(
    val path: String,
    val url: String,
)

data class FailedUpload(
    val path: String,
    val errorMessage: String,
)

@Component
class S3Provider(
    private val s3Template: S3Template,
    @Value("\${spring.cloud.aws.s3.bucket}")
    private val bucket: String,
) {
    private val log = KotlinLogging.logger {}

    /**
     * 파일별로 개별 업로드 처리하여 부분 성공 추적
     * @param imagePaths 로컬 파일 시스템 경로 리스트
     * @return S3UploadResult 성공/실패 정보 포함
     */
    fun uploadImages(imagePaths: List<String>): S3UploadResult {
        val successfulUploads = mutableListOf<SuccessfulUpload>()
        val failedUploads = mutableListOf<FailedUpload>()

        for (path in imagePaths) {
            try {
                val file = File(path)

                if (!file.exists()) {
                    val errorMsg = "파일이 존재하지 않습니다"
                    log.warn { "$errorMsg: $path" }
                    failedUploads.add(FailedUpload(path, errorMsg))
                    continue
                }

                // S3 업로드 (InputStream 자동 Close를 위해 .use 사용)
                val url =
                    file.inputStream().use { inputStream ->
                        val s3Resource =
                            s3Template.upload(
                                bucket,
                                file.name,
                                inputStream,
                                null,
                            )
                        s3Resource.url.toString()
                    }

                successfulUploads.add(SuccessfulUpload(path, url))
                log.debug { "S3 업로드 성공: $path -> $url" }
            } catch (e: Exception) {
                val errorMsg = "${e.javaClass.simpleName}: ${e.message}"
                log.error(e) { "S3 업로드 실패: $path - $errorMsg" }
                failedUploads.add(FailedUpload(path, errorMsg))
            }
        }

        return S3UploadResult(successfulUploads, failedUploads)
    }
}
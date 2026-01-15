package com.few.generator.support.aws

import io.awspring.cloud.s3.S3Template
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File

@Component
class S3Provider(
    private val s3Template: S3Template,
    @Value("\${spring.cloud.aws.s3.bucket}")
    private val bucket: String,
) {
    private val log = KotlinLogging.logger {}

    /**
     * @param imagePaths 로컬 파일 시스템 경로 리스트
     */
    fun uploadImages(imagePaths: List<String>): List<String> {
        return imagePaths
            .map { path ->
                val file = File(path)

                if (!file.exists()) {
                    log.warn { "파일이 존재하지 않습니다: $path" }
                    return@map ""
                }

                // S3 업로드 (InputStream 자동 Close를 위해 .use 사용)
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
            }.filter { it.isNotEmpty() }
    }
}
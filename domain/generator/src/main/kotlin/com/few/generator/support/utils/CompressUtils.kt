package com.few.generator.support.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.*
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object CompressUtils {
    private val log = KotlinLogging.logger {}

    /**
     * GZIP으로 압축하고 Base64로 인코딩하여 문자열로 반환
     */
    fun compress(input: String): String {
        val originalSize = input.toByteArray(Charsets.UTF_8).size

        val byteStream = ByteArrayOutputStream()
        GZIPOutputStream(byteStream).use { it.write(input.toByteArray(Charsets.UTF_8)) }
        val compressedBytes = byteStream.toByteArray()
        val compressedBase64 = Base64.getEncoder().encodeToString(compressedBytes)
        val compressedSize = compressedBase64.toByteArray(Charsets.UTF_8).size

        log.info { "📦 압축 전 크기: $originalSize bytes, 압축 후 크기: $compressedSize bytes" }

        return compressedBase64
    }

    /**
     * GZIP + Base64로 인코딩된 문자열을 디코딩하고 압축을 해제
     */
    fun decompress(compressed: String): String {
        val bytes = Base64.getDecoder().decode(compressed)
        GZIPInputStream(ByteArrayInputStream(bytes)).use { inputStream ->
            return inputStream.reader(Charsets.UTF_8).readText()
        }
    }
}
package com.few.generator.core.instagram

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

data class InstagramResponse(
    val id: String,
)

data class InstagramErrorResponse(
    val error: InstagramError?,
)

data class InstagramError(
    val message: String?,
    val type: String?,
    val code: Int?,
    @SerializedName("fbtrace_id")
    val fbtraceId: String?,
)

@Component
class InstagramUploader(
    @Value("\${generator.instagram.access-token}")
    private val accessToken: String,
    @Value("\${generator.instagram.account-id}")
    private val accountId: String,
    private val instagramOkHttpClient: OkHttpClient,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    // 1단계: 개별 이미지용 컨테이너 생성
    fun createChildMediaContainer(imageUrl: String): String? {
        val url =
            "https://graph.instagram.com/$accountId/media"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("access_token", accessToken)
                ?.addQueryParameter("image_url", imageUrl)
                ?.addQueryParameter("is_carousel_item", "true")
                ?.build()

        val request =
            Request
                .Builder()
                .url(url!!)
                .post(RequestBody.create(null, ""))
                .build()

        instagramOkHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful) {
                val errorResponse = parseErrorResponse(responseBody)
                logErrorResponse("[Step1] Child MediaContainer", response.code, errorResponse)
                throw RuntimeException(
                    "[Instagram][Step1] Creation of Child MediaContainer Failed: ${errorResponse?.error?.message ?: "Unknown error"}",
                )
            }
            Thread.sleep(5000) // TODO: 이미지 컨테이너 올라갔는지 폴링하도록 변경
            return responseBody?.let { parseJsonForId(it).id }
        }
    }

    // 2단계: 캐러셀용 부모 컨테이너 생성
    fun createParentMediaContainer(
        imageUrls: List<String>,
        caption: String,
    ): String? {
        val url =
            "https://graph.instagram.com/$accountId/media"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("access_token", accessToken)
                ?.addQueryParameter("children", imageUrls.joinToString(separator = ","))
                ?.addQueryParameter("caption", caption)
                ?.addQueryParameter("media_type", "CAROUSEL")
                ?.build()

        val request =
            Request
                .Builder()
                .url(url!!)
                .post(RequestBody.create(null, ""))
                .build()

        instagramOkHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful) {
                val errorResponse = parseErrorResponse(responseBody)
                logErrorResponse("[Step2] Parent MediaContainer", response.code, errorResponse)
                throw RuntimeException(
                    "[Instagram][Step2] Creation of Parent MediaContainer Failed: ${errorResponse?.error?.message ?: "Unknown error"}",
                )
            }
            Thread.sleep(5000) // TODO: 이미지 컨테이너 올라갔는지 폴링하도록 변경
            return responseBody?.let { parseJsonForId(it).id }
        }
    }

    // 3단계: 최종 게시
    fun publishMedia(creationId: String): Boolean {
        val url =
            "https://graph.instagram.com/$accountId/media_publish"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("access_token", accessToken)
                ?.addQueryParameter("creation_id", creationId)
                ?.build()

        val request =
            Request
                .Builder()
                .url(url!!)
                .post(RequestBody.create(null, ""))
                .build()

        instagramOkHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful) {
                val errorResponse = parseErrorResponse(responseBody)
                logErrorResponse("[Step3] Publish Media", response.code, errorResponse)
                throw RuntimeException(
                    "[Instagram][Step3] Publishing Media Failed: ${errorResponse?.error?.message ?: "Unknown error"}",
                )
            }
            return response.isSuccessful
        }
    }

    private fun parseJsonForId(json: String): InstagramResponse = gson.fromJson(json, InstagramResponse::class.java)

    private fun parseErrorResponse(responseBody: String?): InstagramErrorResponse? {
        if (responseBody.isNullOrBlank()) return null
        return try {
            gson.fromJson(responseBody, InstagramErrorResponse::class.java)
        } catch (e: Exception) {
            log.warn { "Instagram 에러 응답 파싱 실패: $responseBody" }
            null
        }
    }

    private fun logErrorResponse(
        step: String,
        httpCode: Int,
        errorResponse: InstagramErrorResponse?,
    ) {
        val error = errorResponse?.error
        log.error {
            buildString {
                appendLine("[Instagram]$step Failed")
                appendLine("  HTTP Code: $httpCode")
                if (error != null) {
                    appendLine("  Error Type: ${error.type}")
                    appendLine("  Error Code: ${error.code}")
                    appendLine("  Error Message: ${error.message}")
                    appendLine("  FB Trace ID: ${error.fbtraceId}")
                }
            }
        }
    }
}
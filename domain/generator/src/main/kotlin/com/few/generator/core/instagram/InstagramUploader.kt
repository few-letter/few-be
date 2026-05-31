package com.few.generator.core.instagram

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.service.InstagramTokenService
import com.few.generator.support.utils.DelayUtil
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    private val instagramTokenService: InstagramTokenService,
    @Value("\${generator.instagram.account-id}")
    private val accountId: String,
    private val instagramOkHttpClient: OkHttpClient,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    // 1단계: 개별 이미지용 컨테이너 생성
    suspend fun createChildMediaContainer(imageUrl: String): String? {
        val responseBody =
            suspendCancellableCoroutine { continuation ->
                val url =
                    "https://graph.instagram.com/$accountId/media"
                        .toHttpUrlOrNull()
                        ?.newBuilder()
                        ?.addQueryParameter("access_token", instagramTokenService.getLatestAccessToken())
                        ?.addQueryParameter("image_url", imageUrl)
                        ?.addQueryParameter("is_carousel_item", "true")
                        ?.build()

                val request =
                    Request
                        .Builder()
                        .url(url!!)
                        .post(RequestBody.create(null, ""))
                        .build()

                val call = instagramOkHttpClient.newCall(request)
                continuation.invokeOnCancellation { call.cancel() }

                call.enqueue(
                    object : Callback {
                        override fun onResponse(
                            call: Call,
                            response: Response,
                        ) {
                            response.use {
                                if (!response.isSuccessful) {
                                    val errorResponse = parseErrorResponse(response.body?.string())
                                    logErrorResponse("[Step1] Child MediaContainer", response.code, errorResponse)
                                    continuation.resumeWithException(
                                        RuntimeException(
                                            "[Instagram][Step1] Creation of Child MediaContainer Failed: ${errorResponse?.error?.message ?: "Unknown error"}",
                                        ),
                                    )
                                } else {
                                    continuation.resume(response.body?.string())
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call,
                            e: IOException,
                        ) {
                            continuation.resumeWithException(e)
                        }
                    },
                )
            }

        // TODO: 이미지 컨테이너 올라갔는지 폴링하도록 변경
        DelayUtil.randomDelay(10, 15)
        return responseBody?.let { parseJsonForId(it).id }
    }

    // 2단계: 캐러셀용 부모 컨테이너 생성
    suspend fun createParentMediaContainer(
        imageUrls: List<String>,
        caption: String,
    ): String? {
        val responseBody =
            suspendCancellableCoroutine { continuation ->
                val url =
                    "https://graph.instagram.com/$accountId/media"
                        .toHttpUrlOrNull()
                        ?.newBuilder()
                        ?.addQueryParameter("access_token", instagramTokenService.getLatestAccessToken())
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

                val call = instagramOkHttpClient.newCall(request)
                continuation.invokeOnCancellation { call.cancel() }

                call.enqueue(
                    object : Callback {
                        override fun onResponse(
                            call: Call,
                            response: Response,
                        ) {
                            response.use {
                                if (!response.isSuccessful) {
                                    val errorResponse = parseErrorResponse(response.body?.string())
                                    logErrorResponse("[Step2] Parent MediaContainer", response.code, errorResponse)
                                    continuation.resumeWithException(
                                        RuntimeException(
                                            "[Instagram][Step2] Creation of Parent MediaContainer Failed: ${errorResponse?.error?.message ?: "Unknown error"}",
                                        ),
                                    )
                                } else {
                                    continuation.resume(response.body?.string())
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call,
                            e: IOException,
                        ) {
                            continuation.resumeWithException(e)
                        }
                    },
                )
            }

        // TODO: 이미지 컨테이너 올라갔는지 폴링하도록 변경
        DelayUtil.randomDelay(10, 15)
        return responseBody?.let { parseJsonForId(it).id }
    }

    // 3단계: 최종 게시
    suspend fun publishMedia(creationId: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            val url =
                "https://graph.instagram.com/$accountId/media_publish"
                    .toHttpUrlOrNull()
                    ?.newBuilder()
                    ?.addQueryParameter("access_token", instagramTokenService.getLatestAccessToken())
                    ?.addQueryParameter("creation_id", creationId)
                    ?.build()

            val request =
                Request
                    .Builder()
                    .url(url!!)
                    .post(RequestBody.create(null, ""))
                    .build()

            val call = instagramOkHttpClient.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }

            call.enqueue(
                object : Callback {
                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        response.use {
                            if (!response.isSuccessful) {
                                val errorResponse = parseErrorResponse(response.body?.string())
                                logErrorResponse("[Step3] Publish Media", response.code, errorResponse)
                                continuation.resumeWithException(
                                    RuntimeException(
                                        "[Instagram][Step3] Publishing Media Failed: ${errorResponse?.error?.message ?: "Unknown error"}",
                                    ),
                                )
                            } else {
                                continuation.resume(response.isSuccessful)
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        continuation.resumeWithException(e)
                    }
                },
            )
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
package com.few.generator.core.instagram

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.google.gson.Gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

data class InstagramResponse(
    val id: String,
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
    // 1단계: 미디어 컨테이너 생성 및 Creation ID 획득
    fun createMediaContainer(
        imageUrl: String,
        caption: String,
    ): String? {
        val url =
            "https://graph.instagram.com/$accountId/media"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("image_url", imageUrl)
                ?.addQueryParameter("caption", caption)
                ?.addQueryParameter("access_token", accessToken)
                ?.build()

        val request =
            Request
                .Builder()
                .url(url!!)
                .post(RequestBody.create(null, ""))
                .build()

        instagramOkHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException(
                    "[Instagram] Creation of MediaContainer Failed : HTTP ${response.code} ${response.message} for URL: ${request.url}",
                )
            }
            // JSON 응답에서 "id" 필드를 파싱하세요 (예: {"id": "123456789"})
            return response.body?.string()?.let { parseJsonForId(it).id }
        }
    }

    // 2단계: 최종 게시
    fun publishMedia(creationId: String): Boolean {
        val url =
            "https://graph.instagram.com/$accountId/media_publish"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("creation_id", creationId)
                ?.addQueryParameter("access_token", accessToken)
                ?.build()

        val request =
            Request
                .Builder()
                .url(url!!)
                .post(RequestBody.create(null, ""))
                .build()

        instagramOkHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException(
                    "[Instagram] Publishing Media Failed : HTTP ${response.code} ${response.message} for URL: ${request.url}",
                )
            }
            return response.isSuccessful
        }
    }

    private fun parseJsonForId(json: String): InstagramResponse = gson.fromJson(json, InstagramResponse::class.java)
}
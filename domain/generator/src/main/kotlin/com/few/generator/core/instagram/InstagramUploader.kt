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
            if (!response.isSuccessful) {
                throw RuntimeException(
                    "[Instagram][Step1] Creation of Child MediaContainer Failed : HTTP ${response.code} ${response.message} for URL: ${request.url}",
                )
            }
            // JSON 응답에서 "id" 필드를 파싱하세요 (예: {"id": "123456789"})
            return response.body?.string()?.let { parseJsonForId(it).id }
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
            if (!response.isSuccessful) {
                throw RuntimeException(
                    "[Instagram][Step2] Creation of Parent MediaContainer Failed : HTTP ${response.code} ${response.message} for URL: ${request.url}",
                )
            }
            // JSON 응답에서 "id" 필드를 파싱하세요 (예: {"id": "123456789"})
            return response.body?.string()?.let { parseJsonForId(it).id }
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
            if (!response.isSuccessful) {
                throw RuntimeException(
                    "[Instagram][Step3] Publishing Media Failed : HTTP ${response.code} ${response.message} for URL: ${request.url}",
                )
            }
            return response.isSuccessful
        }
    }

    private fun parseJsonForId(json: String): InstagramResponse = gson.fromJson(json, InstagramResponse::class.java)
}
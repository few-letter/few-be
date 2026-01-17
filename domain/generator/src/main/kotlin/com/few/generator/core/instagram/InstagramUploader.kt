package com.few.generator.core.instagram

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class InstagramUploader(
    @Value("\${generator.instagram.access-token}")
    private val accessToken: String,
    @Value("\${generator.contents.account-id}")
    private val accountId: String,
    private val instagramOkHttpClient: OkHttpClient,
) {
    // 1단계: 미디어 컨테이너 생성 및 Creation ID 획득
    fun createMediaContainer(
        imageUrl: String,
        caption: String,
    ): String? {
        val url =
            "https://graph.facebook.com/v21.0/$accountId/media"
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
            if (!response.isSuccessful) return null
            // JSON 응답에서 "id" 필드를 파싱하세요 (예: {"id": "123456789"})
            return response.body?.string()?.let { parseJsonForId(it) }
        }
    }

    // 2단계: 최종 게시
    fun publishMedia(creationId: String): Boolean {
        val url =
            "https://graph.facebook.com/v21.0/$accountId/media_publish"
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
            return response.isSuccessful
        }
    }

    private fun parseJsonForId(json: String): String {
        // 실제 프로젝트에서는 Gson이나 Kotlinx.serialization을 사용하세요.
        return json.split("\"id\":\"")[1].split("\"")[0]
    }
}
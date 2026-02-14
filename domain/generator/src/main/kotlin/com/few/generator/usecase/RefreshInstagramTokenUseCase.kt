package com.few.generator.usecase

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.service.InstagramTokenService
import com.few.generator.support.jpa.GeneratorTransactional
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

data class InstagramTokenRefreshResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresIn: Long,
)

@Component
class RefreshInstagramTokenUseCase(
    private val instagramTokenService: InstagramTokenService,
    private val instagramOkHttpClient: OkHttpClient,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    @GeneratorTransactional
    fun execute() {
        val currentToken = instagramTokenService.getLatestAccessToken()

        val url =
            "https://graph.instagram.com/refresh_access_token"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("grant_type", "ig_refresh_token")
                ?.addQueryParameter("access_token", currentToken)
                ?.build()

        val request =
            Request
                .Builder()
                .url(url!!)
                .get()
                .build()

        log.info { "Instagram 토큰 갱신 요청 시작" }

        instagramOkHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful) {
                log.error { "Instagram 토큰 갱신 실패 (HTTP ${response.code}): $responseBody" }
                throw RuntimeException("Instagram 토큰 갱신 실패: HTTP ${response.code}")
            }

            val tokenResponse = gson.fromJson(responseBody, InstagramTokenRefreshResponse::class.java)
            instagramTokenService.saveNewToken(tokenResponse.accessToken, tokenResponse.expiresIn)
            log.info { "Instagram 토큰 갱신 성공 (유효기간: ${tokenResponse.expiresIn}초)" }
        }
    }
}
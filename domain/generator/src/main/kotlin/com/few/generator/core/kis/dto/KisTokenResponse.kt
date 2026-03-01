package com.few.generator.core.kis.dto

import com.google.gson.annotations.SerializedName

data class KisTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresIn: Long,
)
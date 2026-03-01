package com.few.generator.core.kis.dto

import com.google.gson.annotations.SerializedName

data class KisTokenRequest(
    @SerializedName("grant_type")
    val grantType: String = "client_credentials",
    @SerializedName("appkey")
    val appKey: String,
    @SerializedName("appsecret")
    val appSecret: String,
)
package com.few.generator.core.alphavantage

import com.google.gson.annotations.SerializedName

data class AlphaVantageNewsFeedItem(
    val title: String,
    val url: String,
    @SerializedName("banner_image") val bannerImage: String?,
    val summary: String,
)
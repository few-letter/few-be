package com.few.common.domain

import com.few.common.exception.BadRequestException

enum class ContentsType(
    val code: Int,
    val title: String,
) {
    NAVER_LOCAL_NEWS(0, "local-news"),
    CNBC_GLOBAL_NEWS(1, "global-news"),
    NAVER_STOCK_BRIEFING(2, "stock-briefing"),
    ALPHAVANTAGE_POPULAR_NASDAQ_STOCK_NEWS(3, "popular-nasdaq-stock-news"),
    INVESTINGCOM_ECONOMIC_ANALYSIS(4, "economic-analysis"),
    INVESTINGCOM_HOT_NEWS(5, "hot-news"),

    ;

    companion object {
        fun from(title: String): ContentsType =
            entries.find { it.title == title }
                ?: throw BadRequestException("Invalid Contents Type: $title")

        fun fromCode(code: Int): ContentsType =
            entries.find { it.code == code }
                ?: throw BadRequestException("Invalid Contents Type Code: $code")
    }
}
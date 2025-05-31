package com.few.generator.domain

import web.handler.exception.BadRequestException

enum class Category(
    val code: Int,
    val title: String,
    val rootUrl: String?,
) {
    TECHNOLOGY(1 shl 1, "기술", "https://news.naver.com/section/105"),
    LIFE(1 shl 2, "생활", "https://news.naver.com/section/103"),
    POLITICS(1 shl 3, "정치", "https://news.naver.com/section/100"),
    ECONOMY(1 shl 4, "경제", "https://news.naver.com/section/101"),
    SOCIETY(1 shl 5, "사회", "https://news.naver.com/section/102"),
    ETC(0, "기타", null),

    ;

    companion object {
        fun from(code: Int): Category =
            Category.values().find { it.code == code }
                ?: throw BadRequestException("Invalid Category code: $code")

        fun from(title: String): Category =
            Category.values().find { it.title.equals(title, ignoreCase = true) }
                ?: throw BadRequestException("Invalid Category title: $title")
    }
}
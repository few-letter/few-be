package com.few.generator.domain

import web.handler.exception.BadRequestException

enum class Category(
    val code: Int,
    val title: String,
) {
    TECHNOLOGY(1 shl 1, "기술"),
    BUSINESS(1 shl 4, "비즈니스"),
    POLITICS(1 shl 7, "정치"),
    ECONOMY(1 shl 9, "경제"),
    APPLE(1 shl 12, "애플"),
    EV(1 shl 13, "전기차"),
    ETC(0, "기타"),

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
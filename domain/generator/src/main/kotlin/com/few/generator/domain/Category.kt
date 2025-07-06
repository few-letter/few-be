package com.few.generator.domain

import web.handler.exception.BadRequestException

enum class Category(
    val code: Int,
    val title: String,
) {
    TECHNOLOGY(1 shl 1, "기술"),
    LIFE(1 shl 2, "생활"),
    POLITICS(1 shl 3, "정치"),
    ECONOMY(1 shl 4, "경제"),
    SOCIETY(1 shl 5, "사회"),
    ETC(0, "기타"),

    ;

    companion object {
        fun from(code: Int): Category =
            Category.entries.find { it.code == code }
                ?: throw BadRequestException("Invalid Category code: $code")

        fun from(title: String): Category =
            Category.entries.find { it.title.equals(title, ignoreCase = true) }
                ?: throw BadRequestException("Invalid Category title: $title")

        fun groupGenEntries(): List<Category> = Category.entries.filter { it.code != Category.ETC.code }
    }
}
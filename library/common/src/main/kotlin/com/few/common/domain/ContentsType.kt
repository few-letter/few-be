package com.few.common.domain

import com.few.common.exception.BadRequestException

enum class ContentsType(
    val code: Int,
    val title: String,
) {
    LOCAL_NEWS(0, "local-news"),
    GLOBAL_NEWS(1, "global-news"),

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
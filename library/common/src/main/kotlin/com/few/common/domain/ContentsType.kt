package com.few.common.domain

import com.few.common.exception.BadRequestException

enum class ContentsType(
    val title: String,
) {
    LOCAL_NEWS("local-news"),
    GLOBAL_NEWS("global-news"),

    ;

    companion object {
        fun from(title: String): ContentsType =
            entries.find { it.title == title }
                ?: throw BadRequestException("Invalid Contents Type: $title")
    }
}
package com.few.common.domain

enum class Region(
    val code: Int,
) {
    LOCAL(0),
    GLOBAL(1),

    ;

    companion object {
        fun from(code: Int): Region = entries.find { it.code == code } ?: LOCAL
    }
}
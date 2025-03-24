package com.few.generator.domain

import com.few.generator.support.common.Constant

enum class GenType(
    val code: Int,
    val title: String,
) {
    STRATEGY_NAME_BASIC(1 shl 1, Constant.GEN.STRATEGY_NAME_BASIC),
    STRATEGY_NAME_KOREAN(1 shl 2, Constant.GEN.STRATEGY_NAME_KOREAN),
    STRATEGY_NAME_KOREAN_QUESTION(1 shl 3, Constant.GEN.STRATEGY_NAME_KOREAN_QUESTION),
    STRATEGY_NAME_KOREAN_LONG_QUESTION(1 shl 4, Constant.GEN.STRATEGY_NAME_KOREAN_LONG_QUESTION),

    ;

    companion object {
        fun from(code: Int): GenType =
            values().find { it.code == code }
                ?: throw IllegalArgumentException("Invalid GEN_TYPE code: $code")

        fun from(title: String): GenType =
            values().find { it.title == title }
                ?: throw IllegalArgumentException("Invalid GEN_TYPE name: $title")
    }
}
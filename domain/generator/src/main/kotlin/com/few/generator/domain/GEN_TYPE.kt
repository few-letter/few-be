package com.few.generator.domain

import com.few.generator.support.common.Constant

enum class GEN_TYPE(
    val code: Int,
    val title: String,
) {
    STRATEGY_NAME_BASIC(2, Constant.GEN.STRATEGY_NAME_BASIC),
    STRATEGY_NAME_KOREAN(4, Constant.GEN.STRATEGY_NAME_KOREAN),
    STRATEGY_NAME_KOREAN_QUESTION(8, Constant.GEN.STRATEGY_NAME_KOREAN_QUESTION),
    STRATEGY_NAME_KOREAN_LONG_QUESTION(16, Constant.GEN.STRATEGY_NAME_KOREAN_LONG_QUESTION),

    ;

    companion object {
        fun from(code: Int): GEN_TYPE =
            values().find { it.code == code }
                ?: throw IllegalArgumentException("Invalid GEN_TYPE code: $code")

        fun from(title: String): GEN_TYPE =
            values().find { it.title == title }
                ?: throw IllegalArgumentException("Invalid GEN_TYPE name: $title")
    }
}
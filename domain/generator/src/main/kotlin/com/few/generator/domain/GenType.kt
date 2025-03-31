package com.few.generator.domain

import com.few.generator.support.common.Constant
import web.handler.exception.BadRequestException

enum class GenType(
    val code: Int,
    val title: String,
) {
    STRATEGY_NAME_QUESTION(1 shl 0, Constant.GEN.STRATEGY_NAME_QUESTION),
    STRATEGY_NAME_LONG(1 shl 1, Constant.GEN.STRATEGY_NAME_LONG),
    STRATEGY_NAME_SHORT(1 shl 2, Constant.GEN.STRATEGY_NAME_SHORT),

    ;

    companion object {
        fun from(code: Int): GenType =
            values().find { it.code == code }
                ?: throw BadRequestException("Invalid GEN_TYPE code: $code")

        fun from(title: String): GenType =
            values().find { it.title == title }
                ?: throw BadRequestException("Invalid GEN_TYPE name: $title")
    }
}
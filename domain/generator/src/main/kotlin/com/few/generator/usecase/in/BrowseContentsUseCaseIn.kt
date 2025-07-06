package com.few.generator.usecase.`in`

import com.few.generator.domain.Category
import web.handler.exception.BadRequestException

data class BrowseContentsUseCaseIn(
    val prevGenId: Long,
    val categoryCode: Int? = null,
) {
    init {
        // 카테고리 코드 유효성 검증
        if (categoryCode != null) {
            try {
                Category.from(categoryCode)
            } catch (e: Exception) {
                throw BadRequestException("Invalid category code: $categoryCode")
            }
        }
    }
}
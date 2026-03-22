package com.few.generator.core.instagram

import java.awt.Color

/**
 * 인스타그램 카드 이미지의 카테고리별 색상 및 설정 상수
 */
data class ColorRGBA(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int = 255,
) {
    fun toColor(): Color = Color(r, g, b, a)

    fun toRGB(): Triple<Int, Int, Int> = Triple(r, g, b)
}

object CategoryConstants {
    // 카테고리별 주요 색상 매핑 (강조 색)
    val CATEGORY_COLOR_MAPPING =
        mapOf(
            "technology" to ColorRGBA(r = 137, g = 49, b = 255), // #8931FF 보라색
            "life" to ColorRGBA(r = 255, g = 160, b = 27), // #FFA01B 주황색
            "politics" to ColorRGBA(r = 17, g = 184, b = 186), // #11B8BA 청록색
            "economy" to ColorRGBA(r = 31, g = 130, b = 249), // #1F82F9 파란색
            "society" to ColorRGBA(r = 253, g = 59, b = 63), // #FD3B3F 빨간색
            "etc" to ColorRGBA(r = 137, g = 49, b = 255),
            "default" to ColorRGBA(r = 137, g = 49, b = 255),
        )

    // 카테고리별 밝은 색상 매핑 (날짜 pill, 장식 요소 등)
    val CATEGORY_LIGHT_COLOR_MAPPING =
        mapOf(
            "technology" to ColorRGBA(r = 200, g = 158, b = 255), // #C89EFF 밝은 보라
            "life" to ColorRGBA(r = 255, g = 203, b = 131), // #FFCB83 밝은 주황
            "politics" to ColorRGBA(r = 123, g = 239, b = 240), // #7BEFF0 밝은 청록
            "economy" to ColorRGBA(r = 107, g = 173, b = 252), // #6BADFC 밝은 파란
            "society" to ColorRGBA(r = 249, g = 149, b = 151), // #F99597 밝은 빨간
            "etc" to ColorRGBA(r = 200, g = 158, b = 255),
            "default" to ColorRGBA(r = 200, g = 158, b = 255),
        )

    // 카테고리별 배경 색상 매핑
    val CATEGORY_BG_COLOR_MAPPING =
        mapOf(
            "technology" to ColorRGBA(r = 242, g = 232, b = 255), // #F2E8FF
            "life" to ColorRGBA(r = 255, g = 244, b = 213), // #FFF4D5
            "politics" to ColorRGBA(r = 231, g = 255, b = 255), // #E7FFFF
            "economy" to ColorRGBA(r = 223, g = 237, b = 255), // #DFEDFF
            "society" to ColorRGBA(r = 255, g = 235, b = 235), // #FFEBEB
            "etc" to ColorRGBA(r = 242, g = 232, b = 255),
            "default" to ColorRGBA(r = 242, g = 232, b = 255),
        )

    // 카테고리별 한글 매핑 (API에서 받은 한글 카테고리를 영문으로 매핑)
    val CATEGORY_KOREAN_TO_ENGLISH_MAPPING =
        mapOf(
            "기술" to "technology",
            "생활" to "life",
            "정치" to "politics",
            "경제" to "economy",
            "사회" to "society",
            "기타" to "etc",
        )

    fun getCategoryColor(category: String): ColorRGBA = CATEGORY_COLOR_MAPPING[category.lowercase()] ?: CATEGORY_COLOR_MAPPING["default"]!!

    fun getCategoryLightColor(category: String): ColorRGBA =
        CATEGORY_LIGHT_COLOR_MAPPING[category.lowercase()] ?: CATEGORY_LIGHT_COLOR_MAPPING["default"]!!

    fun getCategoryBgColor(category: String): ColorRGBA =
        CATEGORY_BG_COLOR_MAPPING[category.lowercase()] ?: CATEGORY_BG_COLOR_MAPPING["default"]!!

    fun getValidCategory(categoryValue: String): String {
        val mappedCategory = CATEGORY_KOREAN_TO_ENGLISH_MAPPING[categoryValue] ?: categoryValue.lowercase()
        return if (mappedCategory in CATEGORY_COLOR_MAPPING) {
            mappedCategory
        } else {
            "technology"
        }
    }
}
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
    // 카테고리별 색상 매핑
    val CATEGORY_COLOR_MAPPING =
        mapOf(
            "technology" to ColorRGBA(r = 137, g = 49, b = 255, a = 255), // 보라색
            "life" to ColorRGBA(r = 255, g = 160, b = 27, a = 255), // 주황색
            "politics" to ColorRGBA(r = 17, g = 184, b = 186, a = 255), // 청록색
            "economy" to ColorRGBA(r = 246, g = 46, b = 149, a = 255), // 핑크색
            "society" to ColorRGBA(r = 255, g = 59, b = 63, a = 255), // 빨간색
            "etc" to ColorRGBA(r = 137, g = 49, b = 255, a = 255), // 기타 - 보라색
            "default" to ColorRGBA(r = 137, g = 49, b = 255, a = 255), // 기본 보라색
        )

    // 카테고리별 텍스트 매핑
    val CATEGORY_TEXT_MAPPING =
        mapOf(
            "technology" to "기술 뉴스",
            "life" to "생활 뉴스",
            "politics" to "정치 뉴스",
            "economy" to "경제 뉴스",
            "society" to "사회 뉴스",
            "etc" to "기타 뉴스",
            "default" to "주목할 뉴스",
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

    // 카테고리별 배경 이미지 매핑 (single_news용)
    val CATEGORY_BG_MAPPING_SINGLE =
        mapOf(
            "technology" to "technology_summary_bg.png",
            "life" to "life_summary_bg.png",
            "politics" to "politics_summary_bg.png",
            "economy" to "economy_summary_bg.png",
            "society" to "society_summary_bg.png",
            "etc" to "technology_summary_bg.png",
            "default" to "technology_summary_bg.png",
        )

    fun getCategoryColor(category: String): ColorRGBA = CATEGORY_COLOR_MAPPING[category.lowercase()] ?: CATEGORY_COLOR_MAPPING["default"]!!

    fun getCategoryText(category: String): String = CATEGORY_TEXT_MAPPING[category.lowercase()] ?: CATEGORY_TEXT_MAPPING["default"]!!

    fun getValidCategory(categoryValue: String): String {
        // 먼저 한글인지 확인하고 영문으로 변환
        val mappedCategory = CATEGORY_KOREAN_TO_ENGLISH_MAPPING[categoryValue] ?: categoryValue.lowercase()

        // 유효한 카테고리인지 확인
        return if (mappedCategory in CATEGORY_COLOR_MAPPING) {
            mappedCategory
        } else {
            "technology" // 기본값
        }
    }
}
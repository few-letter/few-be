package com.few.generator.core.instagram

import com.few.generator.core.instagram.CategoryConstants.getCategoryColor
import com.few.generator.core.instagram.CategoryConstants.getCategoryText
import com.few.generator.core.instagram.CategoryConstants.getValidCategory
import com.few.generator.core.instagram.ImageGeneratorUtils.drawMultilineHighlightedText
import com.few.generator.core.instagram.ImageGeneratorUtils.drawText
import com.few.generator.core.instagram.ImageGeneratorUtils.getWeekdayText
import com.few.generator.core.instagram.ImageGeneratorUtils.loadImageResource
import com.few.generator.core.instagram.ImageGeneratorUtils.loadKoreanFont
import com.few.generator.core.instagram.ImageGeneratorUtils.resizeImage
import com.few.generator.core.instagram.ImageGeneratorUtils.saveImage
import com.few.generator.core.instagram.ImageGeneratorUtils.setupGraphics
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.awt.Graphics2D
import java.awt.image.BufferedImage

/**
 * 단일 뉴스 카드 이미지 생성기 (800 x 950)
 */
@Component
class SingleNewsCardGenerator {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val IMAGE_WIDTH = 800
        private const val IMAGE_HEIGHT = 950
        private const val HEADER_HEIGHT = 87
        private const val MARGIN_X = 60
        private const val MARGIN_TOP = 80
        private const val MARGIN_BOTTOM = 60

        private const val HEADER_FONT_SIZE = 24
        private const val TITLE_FONT_SIZE = 50
        private const val BODY_FONT_SIZE = 28
        private const val SOURCE_FONT_SIZE = 22

        private const val TITLE_LINE_SPACING = 1.2f
        private const val BODY_LINE_SPACING = 1.25f
    }

    /**
     * 단일 뉴스 카드 이미지 생성
     */
    fun generateImage(
        content: NewsContent,
        outputPath: String,
    ): Boolean {
        log.info { "단일 뉴스 카드 이미지 생성 시작: ${content.headline}" }

        val image = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        setupGraphics(graphics)

        try {
            // 배경 그리기
            graphics.color = ImageGeneratorUtils.WHITE_COLOR.toColor()
            graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

            // 카테고리 처리
            val validCategory = getValidCategory(content.category)
            val categoryText = getCategoryText(validCategory)
            val themeColor = getCategoryColor(validCategory)

            // 날짜 문자열 생성
            val dateStr = "${content.createdAt.monthValue}월 ${content.createdAt.dayOfMonth}일 ${
                getWeekdayText(content.createdAt.dayOfWeek.value)
            }"

            // 헤더 배경 그리기
            var currentY = drawHeaderBackground(graphics, validCategory, dateStr, categoryText, themeColor)

            // 제목 그리기
            currentY = drawTitle(graphics, content.headline, currentY, content.highlightTexts, themeColor)

            // 본문 그리기
            currentY = drawBody(graphics, content.summary, currentY, content.highlightTexts, themeColor)

            // 로고 추가
            drawLogo(graphics)

            // 이미지 저장
            return saveImage(image, outputPath)
        } finally {
            graphics.dispose()
        }
    }

    /**
     * 헤더 배경 및 텍스트 그리기
     */
    private fun drawHeaderBackground(
        graphics: Graphics2D,
        category: String,
        dateStr: String,
        categoryText: String,
        themeColor: ColorRGBA,
    ): Int {
        val headerY = MARGIN_TOP

        // 배경 이미지 로드 및 그리기
        val bgFilename = CategoryConstants.CATEGORY_BG_MAPPING_SINGLE[category] ?: "technology_summary_bg.png"
        val bgImage = loadImageResource(bgFilename)

        if (bgImage != null) {
            val resizedBg = resizeImage(bgImage, 580, HEADER_HEIGHT)
            graphics.drawImage(resizedBg, 0, headerY, null)
        } else {
            // 배경 이미지 없을 때 대체 색상
            graphics.color = ImageGeneratorUtils.HEADER_COLOR.toColor()
            graphics.fillRect(0, headerY, 580, HEADER_HEIGHT)
        }

        // 헤더 텍스트 그리기
        val headerFont = loadKoreanFont(HEADER_FONT_SIZE, bold = true)
        val headerTextY = headerY + (HEADER_HEIGHT - HEADER_FONT_SIZE) / 2 + HEADER_FONT_SIZE

        // 날짜 텍스트
        val dateText = "$dateStr "
        drawText(
            graphics,
            dateText,
            MARGIN_X,
            headerTextY,
            headerFont,
            ImageGeneratorUtils.WHITE_COLOR.toColor(),
        )

        // 카테고리 텍스트 (테마 색상)
        val dateWidth = ImageGeneratorUtils.getTextWidth(graphics, dateText, headerFont)
        drawText(
            graphics,
            categoryText,
            MARGIN_X + dateWidth,
            headerTextY,
            headerFont,
            themeColor.toColor(),
        )

        return headerY + HEADER_HEIGHT + 80
    }

    /**
     * 제목 그리기
     */
    private fun drawTitle(
        graphics: Graphics2D,
        title: String,
        currentY: Int,
        highlightTexts: List<String>,
        themeColor: ColorRGBA,
    ): Int {
        if (title.isEmpty()) return currentY

        val titleFont = loadKoreanFont(TITLE_FONT_SIZE, bold = true)
        val maxWidth = IMAGE_WIDTH - (MARGIN_X * 2)

        val endY =
            drawMultilineHighlightedText(
                graphics,
                title,
                highlightTexts,
                MARGIN_X,
                currentY,
                maxWidth,
                titleFont,
                ImageGeneratorUtils.TEXT_COLOR.toColor(),
                themeColor.toColor(),
                TITLE_LINE_SPACING,
            )

        return endY + 30
    }

    /**
     * 본문 그리기
     */
    private fun drawBody(
        graphics: Graphics2D,
        body: String,
        currentY: Int,
        highlightTexts: List<String>,
        themeColor: ColorRGBA,
    ): Int {
        if (body.isEmpty()) return currentY

        val bodyFont = loadKoreanFont(BODY_FONT_SIZE, bold = false)
        val maxWidth = IMAGE_WIDTH - (MARGIN_X * 2)

        val endY =
            drawMultilineHighlightedText(
                graphics,
                body,
                highlightTexts,
                MARGIN_X,
                currentY,
                maxWidth,
                bodyFont,
                ImageGeneratorUtils.TEXT_COLOR.toColor(),
                themeColor.toColor(),
                BODY_LINE_SPACING,
            )

        return endY
    }

    /**
     * 로고 추가
     */
    private fun drawLogo(graphics: Graphics2D) {
        val logoImage = loadImageResource("few_logo.png") ?: return

        // 로고 크기 조정
        val maxSize = 80
        val aspectRatio = logoImage.width.toDouble() / logoImage.height.toDouble()
        val (newWidth, newHeight) =
            if (logoImage.width > logoImage.height) {
                Pair(maxSize, (maxSize / aspectRatio).toInt())
            } else {
                Pair((maxSize * aspectRatio).toInt(), maxSize)
            }

        val resizedLogo = resizeImage(logoImage, newWidth, newHeight)

        // 중앙 하단에 배치
        val logoX = (IMAGE_WIDTH - newWidth) / 2
        val logoY = IMAGE_HEIGHT - MARGIN_BOTTOM - newHeight

        graphics.drawImage(resizedLogo, logoX, logoY, null)
    }
}
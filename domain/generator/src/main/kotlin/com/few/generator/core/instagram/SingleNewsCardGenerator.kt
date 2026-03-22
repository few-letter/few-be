package com.few.generator.core.instagram

import com.few.generator.core.instagram.CategoryConstants.getCategoryBgColor
import com.few.generator.core.instagram.CategoryConstants.getCategoryColor
import com.few.generator.core.instagram.CategoryConstants.getValidCategory
import com.few.generator.core.instagram.ImageGeneratorUtils.drawMultilineHighlightedText
import com.few.generator.core.instagram.ImageGeneratorUtils.loadKoreanFont
import com.few.generator.core.instagram.ImageGeneratorUtils.saveImage
import com.few.generator.core.instagram.ImageGeneratorUtils.setupGraphics
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.image.BufferedImage

/**
 * 단일 뉴스 카드 이미지 생성기 (1080 x 1920)
 */
@Component
class SingleNewsCardGenerator {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val IMAGE_WIDTH = 1080
        private const val IMAGE_HEIGHT = 1920
        private const val MARGIN_X = 80
        private const val CONTENT_WIDTH = 920
        private const val CONTENT_TOP = 320

        private const val HEADLINE_FONT_SIZE = 80
        private const val HEADLINE_LINE_SPACING = 1.25f

        private const val BODY_FONT_SIZE = 40
        private const val BODY_LINE_SPACING = 1.7f

        private const val HEADLINE_BODY_GAP = 80

        private val DARK_TEXT_COLOR = Color(12, 18, 27) // #0C121B
    }

    /**
     * 단일 뉴스 카드 이미지 생성
     */
    fun generateImage(
        content: NewsContent,
        outputPath: String,
    ): Boolean {
        log.debug { "단일 뉴스 카드 이미지 생성 시작: ${content.headline}" }

        val image = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        setupGraphics(graphics)

        try {
            val validCategory = getValidCategory(content.category)
            val themeColor = getCategoryColor(validCategory).toColor()
            val bgColor = getCategoryBgColor(validCategory).toColor()

            // 배경 그리기
            graphics.color = bgColor
            graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

            // 헤드라인 그리기
            val headlineFont = loadKoreanFont(HEADLINE_FONT_SIZE, bold = true)
            graphics.font = headlineFont
            val headlineMetrics = graphics.fontMetrics
            val headlineLineHeight = (headlineMetrics.height * HEADLINE_LINE_SPACING).toInt()
            val headlineStartY = CONTENT_TOP + headlineMetrics.ascent

            val headlineEndY =
                drawMultilineHighlightedText(
                    graphics,
                    content.headline,
                    content.highlightTexts,
                    MARGIN_X,
                    headlineStartY,
                    CONTENT_WIDTH,
                    headlineFont,
                    DARK_TEXT_COLOR,
                    themeColor,
                    HEADLINE_LINE_SPACING,
                )

            // 본문 그리기 (헤드라인 마지막 줄 하단에서 HEADLINE_BODY_GAP 만큼 아래)
            val headlineTextBottom = headlineEndY - headlineLineHeight + headlineMetrics.descent

            val bodyFont = loadKoreanFont(BODY_FONT_SIZE, bold = false)
            graphics.font = bodyFont
            val bodyMetrics = graphics.fontMetrics
            val bodyStartY = headlineTextBottom + HEADLINE_BODY_GAP + bodyMetrics.ascent

            drawMultilineHighlightedText(
                graphics,
                content.summary,
                content.highlightTexts,
                MARGIN_X,
                bodyStartY,
                CONTENT_WIDTH,
                bodyFont,
                DARK_TEXT_COLOR,
                themeColor,
                BODY_LINE_SPACING,
            )

            return saveImage(image, outputPath)
        } finally {
            graphics.dispose()
        }
    }
}
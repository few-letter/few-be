package com.few.generator.core.instagram

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.core.instagram.CategoryConstants.getCategoryBgColor
import com.few.generator.core.instagram.CategoryConstants.getCategoryColor
import com.few.generator.core.instagram.CategoryConstants.getCategoryLightColor
import com.few.generator.core.instagram.CategoryConstants.getValidCategory
import com.few.generator.core.instagram.ImageGeneratorUtils.drawHighlightedText
import com.few.generator.core.instagram.ImageGeneratorUtils.getWeekdayText
import com.few.generator.core.instagram.ImageGeneratorUtils.loadKoreanFont
import com.few.generator.core.instagram.ImageGeneratorUtils.saveImage
import com.few.generator.core.instagram.ImageGeneratorUtils.setupGraphics
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.time.LocalDate

@Component
class MainPageCardGenerator {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val IMAGE_WIDTH = 1080
        private const val IMAGE_HEIGHT = 1920
        private const val HEADER_HEIGHT = 600
        private const val MARGIN_X = 80
        private const val CONTENT_WIDTH = 920

        private val DARK_BG_COLOR = Color(12, 18, 27) // #0C121B

        // 날짜 pill 상수
        private const val PILL_LEFT = 80f
        private const val PILL_TOP = 320f
        private const val PILL_WIDTH = 295f
        private const val PILL_HEIGHT = 74f
        private const val PILL_RADIUS = 135f // 67.5 * 2
        private const val DATE_FONT_SIZE = 36

        // 카테고리 타이틀 상수
        private const val CATEGORY_TITLE_TOP = 402
        private const val CATEGORY_TITLE_FONT_SIZE = 100

        // 헤드라인 목록 상수
        private const val HEADLINES_TOP = 660
        private const val HEADLINE_FONT_SIZE = 48
        private const val HEADLINE_LINE_HEIGHT = 77 // 48px * 160% = 76.8 ≈ 77px
        private const val HEADLINE_GAP = 24
        private const val SEPARATOR_STROKE = 2f
        private const val MAX_HEADLINES = 7
    }

    fun generateMainPageImage(
        category: Category,
        newsContents: List<NewsContent>,
        region: Region,
        outputPath: String,
    ): Boolean {
        log.debug { "[${category.title}] 표지 이미지 생성 시작" }

        val image = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        setupGraphics(graphics)

        try {
            val validCategory = getValidCategory(category.title)
            val primaryColor = getCategoryColor(validCategory).toColor()
            val lightColor = getCategoryLightColor(validCategory).toColor()
            val bgColor = getCategoryBgColor(validCategory).toColor()

            // 밝은 배경 전체 그리기
            graphics.color = bgColor
            graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

            // 상단 어두운 헤더 그리기
            graphics.color = DARK_BG_COLOR
            graphics.fillRect(0, 0, IMAGE_WIDTH, HEADER_HEIGHT)

            // 헤더 우측 장식 요소 그리기
            drawHeaderDecoration(graphics, primaryColor, lightColor)

            // 날짜 pill 그리기
            val today = LocalDate.now()
            val dateStr = "${today.monthValue}월 ${today.dayOfMonth}일 ${getWeekdayText(today.dayOfWeek.value)}"
            drawDatePill(graphics, dateStr, lightColor)

            // 카테고리 타이틀 그리기: "{지역} {카테고리} 소식"
            val regionText =
                when (region) {
                    Region.LOCAL -> "국내"
                    Region.GLOBAL -> "해외"
                }
            drawCategoryTitle(graphics, "$regionText ${category.title} 소식")

            // 헤드라인 목록 그리기
            drawHeadlines(graphics, newsContents.take(MAX_HEADLINES), primaryColor)

            return saveImage(image, outputPath)
        } finally {
            graphics.dispose()
        }
    }

    /**
     * 헤더 우측 장식 요소 그리기
     * CSS: 상단이 둥근 직사각형 두 개 + 세로 바
     */
    private fun drawHeaderDecoration(
        graphics: Graphics2D,
        primaryColor: Color,
        lightColor: Color,
    ) {
        val x = 863f
        val y = 320f
        val outerW = 137f
        val innerW = 62f
        val h = 91f
        val arcSize = 46f // border-radius 23.16px * 2

        // 외부 직사각형 (밝은 색, 상단만 둥글게)
        drawTopRoundedRect(graphics, x, y, outerW, h, arcSize, lightColor)

        // 내부 직사각형 (주요 색, 상단만 둥글게)
        drawTopRoundedRect(graphics, x, y, innerW, h, arcSize, primaryColor)

        // 세로 바 상단 (주요 색) - 헤더 영역 내로 클리핑
        val primaryBarY = 410
        val primaryBarH = minOf(76, HEADER_HEIGHT - primaryBarY)
        graphics.color = primaryColor
        graphics.fillRect(936, primaryBarY, 12, primaryBarH)

        // 세로 바 하단 (밝은 색) - 헤더 영역 내로 클리핑
        val lightBarY = 423
        val lightBarH = minOf(312, HEADER_HEIGHT - lightBarY)
        graphics.color = lightColor
        graphics.fillRect(936, lightBarY, 12, lightBarH)
    }

    /**
     * 상단만 둥근 직사각형 그리기
     */
    private fun drawTopRoundedRect(
        graphics: Graphics2D,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        arcSize: Float,
        color: Color,
    ) {
        graphics.color = color
        // 전체 모서리가 둥근 직사각형 그리기
        val roundRect = RoundRectangle2D.Float(x, y, w, h, arcSize, arcSize)
        graphics.fill(roundRect)
        // 하단 절반을 직사각형으로 덮어 하단 모서리를 직각으로 만들기
        val squareStartY = (y + h / 2).toInt()
        graphics.fillRect(x.toInt(), squareStartY, w.toInt(), (y + h).toInt() - squareStartY + 1)
    }

    /**
     * 날짜 pill 그리기
     */
    private fun drawDatePill(
        graphics: Graphics2D,
        dateStr: String,
        lightColor: Color,
    ) {
        val savedStroke = graphics.stroke
        graphics.color = lightColor
        graphics.stroke = BasicStroke(2.7f)
        val pillRect = RoundRectangle2D.Float(PILL_LEFT, PILL_TOP, PILL_WIDTH, PILL_HEIGHT, PILL_RADIUS, PILL_RADIUS)
        graphics.draw(pillRect)
        graphics.stroke = savedStroke

        val font = loadKoreanFont(DATE_FONT_SIZE, bold = false)
        graphics.font = font
        val metrics = graphics.fontMetrics
        val textX = (PILL_LEFT + (PILL_WIDTH - metrics.stringWidth(dateStr)) / 2).toInt()
        val textY = (PILL_TOP + (PILL_HEIGHT + metrics.ascent - metrics.descent) / 2).toInt()
        graphics.color = lightColor
        graphics.drawString(dateStr, textX, textY)
    }

    /**
     * 카테고리 타이틀 그리기
     */
    private fun drawCategoryTitle(
        graphics: Graphics2D,
        titleText: String,
    ) {
        val font = loadKoreanFont(CATEGORY_TITLE_FONT_SIZE, bold = true)
        graphics.font = font
        val metrics = graphics.fontMetrics
        val textY = CATEGORY_TITLE_TOP + metrics.ascent
        graphics.color = Color.WHITE
        graphics.drawString(titleText, MARGIN_X, textY)
    }

    /**
     * 헤드라인 목록과 구분선 그리기
     */
    private fun drawHeadlines(
        graphics: Graphics2D,
        newsContents: List<NewsContent>,
        primaryColor: Color,
    ) {
        val separatorColor = Color(primaryColor.red, primaryColor.green, primaryColor.blue, 64) // rgba 0.25 alpha
        val headlineFont = loadKoreanFont(HEADLINE_FONT_SIZE, bold = true)
        graphics.font = headlineFont
        val metrics = graphics.fontMetrics

        var currentY = HEADLINES_TOP

        newsContents.forEach { content ->
            val textBaselineY = currentY + metrics.ascent
            drawHighlightedText(
                graphics,
                content.headline,
                content.highlightTexts,
                MARGIN_X,
                textBaselineY,
                headlineFont,
                DARK_BG_COLOR,
                primaryColor,
            )

            val separatorY = currentY + HEADLINE_LINE_HEIGHT + HEADLINE_GAP
            val savedStroke = graphics.stroke
            graphics.color = separatorColor
            graphics.stroke = BasicStroke(SEPARATOR_STROKE)
            graphics.drawLine(MARGIN_X, separatorY, MARGIN_X + CONTENT_WIDTH, separatorY)
            graphics.stroke = savedStroke

            currentY += HEADLINE_LINE_HEIGHT + HEADLINE_GAP + SEPARATOR_STROKE.toInt() + HEADLINE_GAP
        }
    }
}
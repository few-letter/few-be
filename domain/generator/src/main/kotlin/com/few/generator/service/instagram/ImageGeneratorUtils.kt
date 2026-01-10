package com.few.generator.service.instagram

import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * 인스타그램 카드 이미지 생성을 위한 공통 유틸리티 클래스
 */
object ImageGeneratorUtils {
    private val log = KotlinLogging.logger {}

    // 공통 색상 상수
    val THEME_COLOR = ColorRGBA(r = 137, g = 49, b = 255, a = 255)
    val TEXT_COLOR = ColorRGBA(r = 0, g = 0, b = 0, a = 255)
    val WHITE_COLOR = ColorRGBA(r = 255, g = 255, b = 255, a = 255)
    val HEADER_COLOR = ColorRGBA(r = 191, g = 199, b = 212, a = 255)

    /**
     * 리소스에서 이미지 로드
     */
    fun loadImageResource(resourcePath: String): BufferedImage? =
        try {
            val resourceStream = this::class.java.classLoader.getResourceAsStream("images/$resourcePath")
            resourceStream?.use { ImageIO.read(it) }
        } catch (e: Exception) {
            log.warn(e) { "이미지 리소스 로드 실패: $resourcePath" }
            null
        }

    /**
     * 파일에서 이미지 로드
     */
    fun loadImageFile(filePath: String): BufferedImage? =
        try {
            ImageIO.read(File(filePath))
        } catch (e: Exception) {
            log.warn(e) { "이미지 파일 로드 실패: $filePath" }
            null
        }

    /**
     * Graphics2D 초기 설정 (안티앨리어싱 등)
     */
    fun setupGraphics(graphics: Graphics2D) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    }

    /**
     * 한글 폰트 로드 (시스템 폰트 사용)
     */
    fun loadKoreanFont(
        size: Int,
        bold: Boolean = false,
    ): Font {
        val style = if (bold) Font.BOLD else Font.PLAIN

        // macOS용 한글 폰트
        val macFonts =
            listOf(
                "Apple SD Gothic Neo",
                "AppleGothic",
            )

        // 사용 가능한 폰트 찾기
        for (fontName in macFonts) {
            try {
                val font = Font(fontName, style, size)
                if (font.canDisplayUpTo("한글테스트") == -1) {
                    return font
                }
            } catch (e: Exception) {
                continue
            }
        }

        // 기본 폰트 반환 (한글 지원)
        return Font("SansSerif", style, size)
    }

    /**
     * 텍스트 너비 계산
     */
    fun getTextWidth(
        graphics: Graphics2D,
        text: String,
        font: Font,
    ): Int {
        graphics.font = font
        val metrics = graphics.fontMetrics
        return metrics.stringWidth(text)
    }

    /**
     * 텍스트를 여러 줄로 나누기 (한글 지원)
     */
    fun wrapText(
        graphics: Graphics2D,
        text: String,
        font: Font,
        maxWidth: Int,
    ): List<String> {
        if (text.isEmpty()) return emptyList()

        graphics.font = font
        val metrics = graphics.fontMetrics
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (char in text) {
            val testLine = currentLine.toString() + char
            val testWidth = metrics.stringWidth(testLine)

            if (testWidth > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder().append(char)
            } else {
                currentLine.append(char)
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }

    /**
     * 단일 라인 텍스트 그리기
     */
    fun drawText(
        graphics: Graphics2D,
        text: String,
        x: Int,
        y: Int,
        font: Font,
        color: Color,
    ) {
        graphics.font = font
        graphics.color = color
        graphics.drawString(text, x, y)
    }

    /**
     * 하이라이트된 텍스트 그리기
     */
    fun drawHighlightedText(
        graphics: Graphics2D,
        text: String,
        highlightTexts: List<String>,
        x: Int,
        y: Int,
        font: Font,
        normalColor: Color,
        highlightColor: Color,
    ): Int {
        if (highlightTexts.isEmpty()) {
            drawText(graphics, text, x, y, font, normalColor)
            return getTextWidth(graphics, text, font)
        }

        graphics.font = font
        val metrics = graphics.fontMetrics
        var currentX = x
        var remainingText = text

        while (remainingText.isNotEmpty()) {
            var foundHighlight = false

            // 하이라이트 텍스트 찾기
            for (highlight in highlightTexts) {
                if (remainingText.startsWith(highlight)) {
                    graphics.color = highlightColor
                    graphics.drawString(highlight, currentX, y)
                    currentX += metrics.stringWidth(highlight)
                    remainingText = remainingText.substring(highlight.length)
                    foundHighlight = true
                    break
                }
            }

            if (!foundHighlight) {
                // 다음 하이라이트 위치 찾기
                var nextHighlightPos = remainingText.length
                for (highlight in highlightTexts) {
                    val pos = remainingText.indexOf(highlight)
                    if (pos >= 0 && pos < nextHighlightPos) {
                        nextHighlightPos = pos
                    }
                }

                // 일반 텍스트 그리기
                val normalText = remainingText.substring(0, nextHighlightPos)
                graphics.color = normalColor
                graphics.drawString(normalText, currentX, y)
                currentX += metrics.stringWidth(normalText)
                remainingText = remainingText.substring(nextHighlightPos)
            }
        }

        return currentX - x
    }

    /**
     * 여러 줄 텍스트 그리기
     */
    fun drawMultilineText(
        graphics: Graphics2D,
        text: String,
        x: Int,
        startY: Int,
        maxWidth: Int,
        font: Font,
        color: Color,
        lineSpacing: Float = 1.4f,
    ): Int {
        val lines = wrapText(graphics, text, font, maxWidth)
        graphics.font = font
        val metrics = graphics.fontMetrics
        val lineHeight = (metrics.height * lineSpacing).toInt()

        var currentY = startY
        for (line in lines) {
            drawText(graphics, line, x, currentY, font, color)
            currentY += lineHeight
        }

        return currentY
    }

    /**
     * 하이라이트 지원 여러 줄 텍스트 그리기
     */
    fun drawMultilineHighlightedText(
        graphics: Graphics2D,
        text: String,
        highlightTexts: List<String>,
        x: Int,
        startY: Int,
        maxWidth: Int,
        font: Font,
        normalColor: Color,
        highlightColor: Color,
        lineSpacing: Float = 1.4f,
    ): Int {
        val lines = wrapText(graphics, text, font, maxWidth)
        graphics.font = font
        val metrics = graphics.fontMetrics
        val lineHeight = (metrics.height * lineSpacing).toInt()

        var currentY = startY
        for (line in lines) {
            drawHighlightedText(
                graphics,
                line,
                highlightTexts,
                x,
                currentY,
                font,
                normalColor,
                highlightColor,
            )
            currentY += lineHeight
        }

        return currentY
    }

    /**
     * 이미지를 중앙 정렬하여 그리기
     */
    fun drawImageCentered(
        graphics: Graphics2D,
        image: BufferedImage,
        canvasWidth: Int,
        y: Int,
    ) {
        val x = (canvasWidth - image.width) / 2
        graphics.drawImage(image, x, y, null)
    }

    /**
     * 이미지 크기 조정
     */
    fun resizeImage(
        image: BufferedImage,
        targetWidth: Int,
        targetHeight: Int,
    ): BufferedImage {
        val resized = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = resized.createGraphics()
        setupGraphics(graphics)
        graphics.drawImage(
            image,
            0,
            0,
            targetWidth,
            targetHeight,
            0,
            0,
            image.width,
            image.height,
            null,
        )
        graphics.dispose()
        return resized
    }

    /**
     * 이미지 저장
     */
    fun saveImage(
        image: BufferedImage,
        outputPath: String,
    ): Boolean =
        try {
            val outputFile = File(outputPath)
            outputFile.parentFile?.mkdirs()
            ImageIO.write(image, "PNG", outputFile)
            log.info { "이미지 저장 완료: $outputPath" }
            true
        } catch (e: Exception) {
            log.error(e) { "이미지 저장 실패: $outputPath" }
            false
        }

    /**
     * 요일 텍스트 반환
     */
    fun getWeekdayText(dayOfWeek: Int): String {
        val weekdays = listOf("월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일")
        val index = (dayOfWeek - 1) % 7
        return weekdays.getOrElse(index) { "월요일" }
    }
}
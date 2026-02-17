package com.few.generator.core.instagram

import com.few.common.domain.Category
import com.few.generator.core.instagram.ImageGeneratorUtils.drawText
import com.few.generator.core.instagram.ImageGeneratorUtils.loadImageResource
import com.few.generator.core.instagram.ImageGeneratorUtils.loadKoreanFont
import com.few.generator.core.instagram.ImageGeneratorUtils.saveImage
import com.few.generator.core.instagram.ImageGeneratorUtils.setupGraphics
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.image.BufferedImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Component
class MainPageCardGenerator {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val TEMPLATE_IMAGE = "card_news_main_page.png"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
    }

    fun generateMainPageImage(
        category: Category,
        outputPath: String,
    ): Boolean {
        log.debug { "[${category.title}] 표지 이미지 생성 시작" }

        val templateImage = loadImageResource(TEMPLATE_IMAGE)
        if (templateImage == null) {
            log.error { "표지 템플릿 이미지 로드 실패: $TEMPLATE_IMAGE" }
            return false
        }

        val width = templateImage.width
        val height = templateImage.height

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        setupGraphics(graphics)

        try {
            graphics.drawImage(templateImage, 0, 0, null)

            val text = "${LocalDate.now().format(DATE_FORMATTER)} | ${category.title} 소식"
            val fontSize = (height * 0.025).toInt()
            val font = loadKoreanFont(fontSize, bold = true)

            val x = (width * 0.9 / 11).toInt()
            val y = (height * 1.02 / 3).toInt()

            drawText(graphics, text, x, y, font, Color.WHITE)

            return saveImage(image, outputPath)
        } finally {
            graphics.dispose()
        }
    }
}
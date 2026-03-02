package com.few.generator.core.instagram

import com.few.generator.core.instagram.ImageGeneratorUtils.drawText
import com.few.generator.core.instagram.ImageGeneratorUtils.loadImageResource
import com.few.generator.core.instagram.ImageGeneratorUtils.loadKoreanFont
import com.few.generator.core.instagram.ImageGeneratorUtils.saveImage
import com.few.generator.core.instagram.ImageGeneratorUtils.setupGraphics
import com.few.generator.core.kis.OverseaStockConstants
import com.few.generator.core.kis.StockQuote
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 나스닥 주요 종목 주식 카드 이미지 생성기 (800 x 950)
 *
 * 레이아웃:
 *  Y=  0 ~  87 : 헤더바 (보라색) - "나스닥 주요 종목 | 날짜"
 *  Y= 87 ~ 117 : ETF 섹션 레이블
 *  Y=117 ~ 217 : ETF 종목 2개 수평 배치 (koreanName 위 / changeRate 아래)
 *  Y=217 ~ 232 : 섹션 구분 여백
 *  Y=232 ~ 262 : M7 섹션 레이블
 *  Y=262 ~ 822 : M7 종목 행 × 7 (행 높이 80px, 로고 + koreanName + price + changeRate)
 *  Y=822 ~ 842 : 로고 상단 여백
 *  Y=842 ~ 892 : few 로고
 *  Y=892 ~ 950 : 하단 여백
 */
@Component
class StockCardGenerator {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val IMAGE_WIDTH = 800
        private const val IMAGE_HEIGHT = 950

        private const val HEADER_HEIGHT = 87
        private const val HEADER_FONT_SIZE = 24

        private const val SECTION_LABEL_HEIGHT = 30
        private const val SECTION_LABEL_FONT_SIZE = 14
        private const val SECTION_ACCENT_WIDTH = 5

        // ETF 섹션
        private const val ETF_SECTION_Y = HEADER_HEIGHT // 87
        private const val ETF_ROW_START_Y = ETF_SECTION_Y + SECTION_LABEL_HEIGHT // 117
        private const val ETF_CONTENT_HEIGHT = 100
        private const val ETF_NAME_Y_OFFSET = 34  // koreanName Y = ETF_ROW_START_Y + 34
        private const val ETF_RATE_Y_OFFSET = 74  // changeRate Y = ETF_ROW_START_Y + 74

        // M7 섹션
        private const val M7_SECTION_GAP = 15
        private const val M7_SECTION_Y = ETF_ROW_START_Y + ETF_CONTENT_HEIGHT + M7_SECTION_GAP // 232
        private const val M7_ROW_START_Y = M7_SECTION_Y + SECTION_LABEL_HEIGHT // 262
        private const val M7_ROW_HEIGHT = 80
        private const val COMPANY_LOGO_SIZE = 44

        private const val MARGIN_X = 40

        // M7 컬럼 (symbol 제거, koreanName이 logo 바로 옆에 위치)
        private const val M7_COL_LOGO = 40
        private const val M7_COL_KOREAN_NAME = 96  // 40 + 44 + 12
        private const val M7_COL_PRICE = 440
        private const val M7_COL_CHANGE_RATE = 615

        private const val NAME_FONT_SIZE = 24   // koreanName: symbol과 동일한 굵은 스타일
        private const val PRICE_FONT_SIZE = 22

        // few 로고
        private const val FEW_LOGO_Y = M7_ROW_START_Y + M7_ROW_HEIGHT * 7 + 20 // 842
        private const val FEW_LOGO_MAX_SIZE = 50

        // 등락 색상
        private val RISE_COLOR = Color(255, 59, 63)
        private val FALL_COLOR = Color(56, 118, 229)
        private val NEUTRAL_COLOR = Color(130, 130, 130)
        private val DIVIDER_COLOR = Color(230, 230, 230)
        private val SECTION_BG_COLOR = Color(245, 240, 255)

        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("M월 d일 E요일", Locale.KOREAN)

        /** 종목별 브랜드 색상 (로고 이미지 없을 때 원형 배지에 사용) */
        private val BRAND_COLORS: Map<String, Color> =
            mapOf(
                "AAPL" to Color(100, 100, 100),
                "MSFT" to Color(0, 164, 239),
                "GOOGL" to Color(66, 133, 244),
                "AMZN" to Color(255, 153, 0),
                "NVDA" to Color(118, 185, 0),
                "META" to Color(8, 102, 255),
                "TSLA" to Color(204, 0, 0),
            )
    }

    fun generateImage(
        stocks: Map<OverseaStockConstants.StockGroup, List<StockQuote>>,
        outputPath: String,
        date: LocalDate = LocalDate.now(),
    ): Boolean {
        log.debug { "나스닥 주식 카드 이미지 생성 시작 (종목 수: ${stocks.values.sumOf { it.size }})" }

        val image = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        setupGraphics(graphics)

        try {
            graphics.color = ImageGeneratorUtils.WHITE_COLOR.toColor()
            graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

            drawHeader(graphics, date)

            val etfStocks = stocks[OverseaStockConstants.StockGroup.ETF] ?: emptyList()
            val m7Stocks = stocks[OverseaStockConstants.StockGroup.M7] ?: emptyList()

            drawSectionLabel(graphics, "ETF", ETF_SECTION_Y)
            drawEtfSection(graphics, etfStocks)

            drawSectionLabel(graphics, "Magnificent 7", M7_SECTION_Y)
            drawM7Rows(graphics, m7Stocks)

            drawFewLogo(graphics)

            return saveImage(image, outputPath)
        } finally {
            graphics.dispose()
        }
    }

    private fun drawHeader(
        graphics: Graphics2D,
        date: LocalDate,
    ) {
        graphics.color = ImageGeneratorUtils.THEME_COLOR.toColor()
        graphics.fillRect(0, 0, IMAGE_WIDTH, HEADER_HEIGHT)

        val font = loadKoreanFont(HEADER_FONT_SIZE, bold = true)
        val textY = (HEADER_HEIGHT + HEADER_FONT_SIZE) / 2

        val title = "나스닥 주요 종목"
        drawText(graphics, title, MARGIN_X, textY, font, ImageGeneratorUtils.WHITE_COLOR.toColor())

        val dateStr = date.format(DATE_FORMATTER)
        val titleWidth = ImageGeneratorUtils.getTextWidth(graphics, title, font)
        val separator = "  |  "
        val separatorWidth = ImageGeneratorUtils.getTextWidth(graphics, separator, font)
        drawText(graphics, separator, MARGIN_X + titleWidth, textY, font, Color(200, 180, 255))
        drawText(graphics, dateStr, MARGIN_X + titleWidth + separatorWidth, textY, font, ImageGeneratorUtils.WHITE_COLOR.toColor())
    }

    private fun drawSectionLabel(
        graphics: Graphics2D,
        label: String,
        sectionY: Int,
    ) {
        graphics.color = SECTION_BG_COLOR
        graphics.fillRect(0, sectionY, IMAGE_WIDTH, SECTION_LABEL_HEIGHT)

        graphics.color = ImageGeneratorUtils.THEME_COLOR.toColor()
        graphics.fillRect(0, sectionY, SECTION_ACCENT_WIDTH, SECTION_LABEL_HEIGHT)

        val font = loadKoreanFont(SECTION_LABEL_FONT_SIZE, bold = true)
        val textY = sectionY + (SECTION_LABEL_HEIGHT + SECTION_LABEL_FONT_SIZE) / 2
        drawText(graphics, label, MARGIN_X, textY, font, ImageGeneratorUtils.THEME_COLOR.toColor())
    }

    /**
     * ETF 종목 수평 배치. ETF 개수에 따라 너비를 동적으로 분할.
     * 각 카드: koreanName (위) → changeRate (아래)
     * 카드 사이에 수직 구분선.
     */
    private fun drawEtfSection(
        graphics: Graphics2D,
        stocks: List<StockQuote>,
    ) {
        if (stocks.isEmpty()) return

        val nameFont = loadKoreanFont(NAME_FONT_SIZE, bold = true)
        val rateFont = loadKoreanFont(PRICE_FONT_SIZE, bold = true)

        val cardWidth = IMAGE_WIDTH / stocks.size
        val nameY = ETF_ROW_START_Y + ETF_NAME_Y_OFFSET
        val rateY = ETF_ROW_START_Y + ETF_RATE_Y_OFFSET

        stocks.forEachIndexed { index, stock ->
            val textX = index * cardWidth + MARGIN_X
            drawText(graphics, stock.koreanName, textX, nameY, nameFont, ImageGeneratorUtils.TEXT_COLOR.toColor())

            val (arrow, changeColor) = changeArrow(stock)
            drawText(graphics, "$arrow ${stock.changeRate}%", textX, rateY, rateFont, changeColor)
        }

        // 카드 사이 수직 구분선
        graphics.color = DIVIDER_COLOR
        graphics.stroke = BasicStroke(1f)
        repeat(stocks.size - 1) { i ->
            val dividerX = cardWidth * (i + 1)
            graphics.drawLine(dividerX, ETF_ROW_START_Y, dividerX, ETF_ROW_START_Y + ETF_CONTENT_HEIGHT)
        }
    }

    /**
     * M7 종목 행: 회사 로고 + koreanName (bold) + 현재가 + 등락률
     */
    private fun drawM7Rows(
        graphics: Graphics2D,
        stocks: List<StockQuote>,
    ) {
        val nameFont = loadKoreanFont(NAME_FONT_SIZE, bold = true)
        val priceFont = loadKoreanFont(PRICE_FONT_SIZE, bold = true)

        stocks.forEachIndexed { index, stock ->
            val rowY = M7_ROW_START_Y + index * M7_ROW_HEIGHT
            val textY = rowY + (M7_ROW_HEIGHT + PRICE_FONT_SIZE) / 2
            val logoY = rowY + (M7_ROW_HEIGHT - COMPANY_LOGO_SIZE) / 2

            drawCompanyLogo(graphics, stock.symbol, M7_COL_LOGO, logoY, COMPANY_LOGO_SIZE)
            drawText(graphics, stock.koreanName, M7_COL_KOREAN_NAME, textY, nameFont, ImageGeneratorUtils.TEXT_COLOR.toColor())
            drawText(graphics, "$${stock.currentPrice}", M7_COL_PRICE, textY, priceFont, ImageGeneratorUtils.TEXT_COLOR.toColor())

            val (arrow, changeColor) = changeArrow(stock)
            drawText(graphics, "$arrow ${stock.changeRate}%", M7_COL_CHANGE_RATE, textY, priceFont, changeColor)

            if (index < stocks.size - 1) {
                drawDivider(graphics, rowY + M7_ROW_HEIGHT)
            }
        }
    }

    private fun drawCompanyLogo(
        graphics: Graphics2D,
        symbol: String,
        x: Int,
        y: Int,
        size: Int,
    ) {
        val logoImage = loadImageResource("${symbol.lowercase()}_logo.png")
        if (logoImage != null) {
            val resized = ImageGeneratorUtils.resizeImage(logoImage, size, size)
            graphics.drawImage(resized, x, y, null)
        } else {
            drawCompanyBadge(graphics, symbol, x, y, size)
        }
    }

    private fun drawCompanyBadge(
        graphics: Graphics2D,
        symbol: String,
        x: Int,
        y: Int,
        size: Int,
    ) {
        val brandColor = BRAND_COLORS[symbol] ?: ImageGeneratorUtils.THEME_COLOR.toColor()

        graphics.color = brandColor
        graphics.fillOval(x, y, size, size)

        val font = loadKoreanFont((size * 0.42).toInt(), bold = true)
        val letter = symbol.first().toString()
        graphics.font = font
        val metrics = graphics.fontMetrics
        val letterX = x + (size - metrics.stringWidth(letter)) / 2
        val letterY = y + (size + metrics.ascent - metrics.descent) / 2
        graphics.color = Color.WHITE
        graphics.drawString(letter, letterX, letterY)
    }

    private fun drawDivider(
        graphics: Graphics2D,
        dividerY: Int,
    ) {
        graphics.color = DIVIDER_COLOR
        graphics.stroke = BasicStroke(1f)
        graphics.drawLine(MARGIN_X, dividerY, IMAGE_WIDTH - MARGIN_X, dividerY)
    }

    private fun changeArrow(stock: StockQuote): Pair<String, Color> =
        when (stock.isRise) {
            true -> "▲" to RISE_COLOR
            false -> "▼" to FALL_COLOR
            null -> "-" to NEUTRAL_COLOR
        }

    private fun drawFewLogo(graphics: Graphics2D) {
        val logoImage = loadImageResource("few_logo.png") ?: return

        val aspectRatio = logoImage.width.toDouble() / logoImage.height.toDouble()
        val (newWidth, newHeight) =
            if (logoImage.width > logoImage.height) {
                Pair(FEW_LOGO_MAX_SIZE, (FEW_LOGO_MAX_SIZE / aspectRatio).toInt())
            } else {
                Pair((FEW_LOGO_MAX_SIZE * aspectRatio).toInt(), FEW_LOGO_MAX_SIZE)
            }

        val resizedLogo = ImageGeneratorUtils.resizeImage(logoImage, newWidth, newHeight)
        val logoX = (IMAGE_WIDTH - newWidth) / 2
        graphics.drawImage(resizedLogo, logoX, FEW_LOGO_Y, null)
    }
}

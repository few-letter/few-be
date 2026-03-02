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
import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 나스닥 주요 종목 주식 카드 이미지 생성기 (1080 × 1080 – 1:1 인스타그램)
 *
 * 레이아웃:
 *  Y=   0 ~  120 : 헤더 (딥네이비 #1F2333) – "NASDAQ DAILY" + 날짜
 *  Y= 120 ~  320 : ETF 라운드 카드 3개 수평 배치 (상단 패딩 30 + 카드 160 + 하단 간격 10)
 *  Y= 320 ~  375 : M7 섹션 타이틀 (아쿠아 + 밑줄)
 *  Y= 375 ~  970 : M7 종목 7행 × 85px (로고 | 이름 | 가격 | 등락률), 세로 중앙 정렬
 *  Y= 975 ~ 1080 : 푸터 – Market Mood (선택) + few_logo.png
 */
@Component
class NasdaqDailyStockCardGenerator {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val IMAGE_WIDTH = 1080
        private const val IMAGE_HEIGHT = 1080

        // ── Header ───────────────────────────────────────────────────────────
        private const val HEADER_HEIGHT = 120
        private const val HEADER_TITLE_X = 100

        // ── ETF ──────────────────────────────────────────────────────────────
        private const val ETF_SECTION_Y = HEADER_HEIGHT // 120
        private const val ETF_CARDS_Y = ETF_SECTION_Y + 30 // 150
        private const val ETF_CARD_HEIGHT = 160
        private const val ETF_CARD_CORNER = 20
        private const val ETF_SIDE_MARGIN = 30
        private const val ETF_CARD_GAP = 12

        // ── M7 ───────────────────────────────────────────────────────────────
        private const val M7_SECTION_Y = ETF_CARDS_Y + ETF_CARD_HEIGHT + 10 // 320
        private const val M7_TITLE_AREA_H = 55
        private const val M7_TITLE_X = 100
        private const val M7_ROW_HEIGHT = 85

        // M7 row column positions
        private const val COMPANY_LOGO_SIZE = 44
        private const val M7_COL_LOGO = 100
        private const val M7_COL_NAME = M7_COL_LOGO + COMPANY_LOGO_SIZE + 20 // 114
        private const val M7_COL_PRICE = 610
        private const val M7_COL_CHANGE = 850

        // ── Footer ────────────────────────────────────────────────────────────
        private const val FOOTER_START_Y = 975
        private const val FEW_LOGO_MAX_SIZE = 40

        // ── Font sizes ────────────────────────────────────────────────────────
        private const val NAME_FONT_SIZE = 22
        private const val PRICE_FONT_SIZE = 20
        private const val CHANGE_FONT_SIZE = 20

        // ── Colors ────────────────────────────────────────────────────────────
        private val HEADER_BG_COLOR = Color(0x1F, 0x23, 0x33)
        private val AQUA_BLUE = Color(0x63, 0xC7, 0xE6)
        private val BG_COLOR = Color(0xF4, 0xF6, 0xF8)
        private val RISE_COLOR = Color(0xE0, 0x57, 0x57)
        private val FALL_COLOR = Color(0x2C, 0x4A, 0x6E)
        private val NEUTRAL_COLOR = Color(0x99, 0x99, 0x99)
        private val TEXT_COLOR = Color(0x33, 0x33, 0x33)
        private val DIVIDER_COLOR = Color(0xE0, 0xE4, 0xE8)

        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("M월 d일  |  E요일", Locale.KOREAN)

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
        marketMood: String = "",
    ): Boolean {
        log.debug { "나스닥 주식 카드 이미지 생성 시작 (종목 수: ${stocks.values.sumOf { it.size }})" }

        val image = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        setupGraphics(graphics)

        try {
            graphics.color = BG_COLOR
            graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

            drawHeader(graphics, date)

            val etfStocks = stocks[OverseaStockConstants.StockGroup.ETF] ?: emptyList()
            val m7Stocks = stocks[OverseaStockConstants.StockGroup.M7] ?: emptyList()

            drawEtfCards(graphics, etfStocks)
            drawM7Section(graphics, m7Stocks)
            drawFooter(graphics, marketMood)

            return saveImage(image, outputPath)
        } finally {
            graphics.dispose()
        }
    }

    private fun drawHeader(
        graphics: Graphics2D,
        date: LocalDate,
    ) {
        graphics.color = HEADER_BG_COLOR
        graphics.fillRect(0, 0, IMAGE_WIDTH, HEADER_HEIGHT)

        val labelFont = loadKoreanFont(32, bold = true)
        val dateFont = loadKoreanFont(20, bold = false)

        drawText(graphics, "NASDAQ DAILY", HEADER_TITLE_X, 55, labelFont, AQUA_BLUE)

        val orig = graphics.composite
        graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f)
        drawText(graphics, date.format(DATE_FORMATTER), HEADER_TITLE_X, 95, dateFont, Color.WHITE)
        graphics.composite = orig
    }

    /**
     * ETF 종목을 라운드 흰색 카드 3개로 수평 배치.
     * 각 카드: 인덱스명 (위) + 등락률 (아래), 모두 중앙 정렬.
     */
    private fun drawEtfCards(
        graphics: Graphics2D,
        stocks: List<StockQuote>,
    ) {
        if (stocks.isEmpty()) return

        val n = stocks.size
        val totalCardWidth = IMAGE_WIDTH - ETF_SIDE_MARGIN * 2 - ETF_CARD_GAP * (n - 1)
        val cardWidth = totalCardWidth / n

        val nameFont = loadKoreanFont(32, bold = true)
        val rateFont = loadKoreanFont(24, bold = true)

        // 두 텍스트 블록을 카드 높이 기준으로 세로 중앙 정렬
        graphics.font = nameFont
        val nameMetrics = graphics.fontMetrics
        graphics.font = rateFont
        val rateMetrics = graphics.fontMetrics
        val textGap = 10
        val blockH = nameMetrics.height + textGap + rateMetrics.height
        val blockStartY = ETF_CARDS_Y + (ETF_CARD_HEIGHT - blockH) / 2
        val nameBaselineY = blockStartY + nameMetrics.ascent
        val rateBaselineY = blockStartY + nameMetrics.height + textGap + rateMetrics.ascent

        stocks.forEachIndexed { i, stock ->
            val cardX = ETF_SIDE_MARGIN + i * (cardWidth + ETF_CARD_GAP)

            // Subtle drop shadow
            val origComposite = graphics.composite
            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f)
            graphics.color = Color.BLACK
            graphics.fillRoundRect(cardX + 2, ETF_CARDS_Y + 4, cardWidth, ETF_CARD_HEIGHT, ETF_CARD_CORNER, ETF_CARD_CORNER)
            graphics.composite = origComposite

            // Card background
            graphics.color = Color.WHITE
            graphics.fillRoundRect(cardX, ETF_CARDS_Y, cardWidth, ETF_CARD_HEIGHT, ETF_CARD_CORNER, ETF_CARD_CORNER)

            // Index name – horizontally centered, vertically centered
            graphics.font = nameFont
            val nameW = graphics.fontMetrics.stringWidth(stock.koreanName)
            val nameX = cardX + (cardWidth - nameW) / 2
            drawText(graphics, stock.koreanName, nameX, nameBaselineY, nameFont, TEXT_COLOR)

            // Change rate – horizontally centered, vertically centered
            val (arrow, changeColor) = changeArrow(stock)
            val rateText = "$arrow ${stock.changeRate}%"
            graphics.font = rateFont
            val rateW = graphics.fontMetrics.stringWidth(rateText)
            val rateX = cardX + (cardWidth - rateW) / 2
            drawText(graphics, rateText, rateX, rateBaselineY, rateFont, changeColor)
        }
    }

    /**
     * M7 섹션: "M7" 타이틀 + 아쿠아 밑줄 + 7개 종목 행.
     * 각 행: 한국어 종목명 | 현재가 | 등락률 | 우측 로고.
     */
    private fun drawM7Section(
        graphics: Graphics2D,
        stocks: List<StockQuote>,
    ) {
        // Title
        val titleFont = loadKoreanFont(28, bold = true)
        val titleTextY = M7_SECTION_Y + 40
        drawText(graphics, "M7", M7_TITLE_X, titleTextY, titleFont, AQUA_BLUE)

        // Underline
        val titleW = ImageGeneratorUtils.getTextWidth(graphics, "M7", titleFont)
        graphics.color = AQUA_BLUE
        graphics.stroke = BasicStroke(2f)
        graphics.drawLine(M7_TITLE_X, titleTextY + 6, M7_TITLE_X + titleW, titleTextY + 6)

        // Center rows vertically in available space
        val totalRowsH = stocks.size * M7_ROW_HEIGHT
        val available = FOOTER_START_Y - M7_SECTION_Y - M7_TITLE_AREA_H
        val rowsStartY = M7_SECTION_Y + M7_TITLE_AREA_H + (available - totalRowsH).coerceAtLeast(0) / 2

        val nameFont = loadKoreanFont(NAME_FONT_SIZE, bold = true)
        val priceFont = loadKoreanFont(PRICE_FONT_SIZE, bold = false)
        val changeFont = loadKoreanFont(CHANGE_FONT_SIZE, bold = true)

        stocks.forEachIndexed { idx, stock ->
            val rowY = rowsStartY + idx * M7_ROW_HEIGHT
            val textY = rowY + (M7_ROW_HEIGHT + PRICE_FONT_SIZE) / 2
            val logoY = rowY + (M7_ROW_HEIGHT - COMPANY_LOGO_SIZE) / 2

            val (arrow, changeColor) = changeArrow(stock)

            // Korean company name
            drawText(graphics, stock.koreanName, M7_COL_NAME, textY, nameFont, TEXT_COLOR)

            // Current price
            drawText(graphics, "$${stock.currentPrice}", M7_COL_PRICE, textY, priceFont, TEXT_COLOR)

            // Change rate with direction arrow
            drawText(graphics, "$arrow ${stock.changeRate}%", M7_COL_CHANGE, textY, changeFont, changeColor)

            // Company logo (far right)
            drawCompanyLogo(graphics, stock.symbol, M7_COL_LOGO, logoY, COMPANY_LOGO_SIZE)

            // Row divider (skip last row)
            if (idx < stocks.size - 1) {
                graphics.color = DIVIDER_COLOR
                graphics.stroke = BasicStroke(1f)
                graphics.drawLine(40, rowY + M7_ROW_HEIGHT, IMAGE_WIDTH - 40, rowY + M7_ROW_HEIGHT)
            }
        }
    }

    private fun drawFooter(
        graphics: Graphics2D,
        marketMood: String,
    ) {
        // Thin separator line
        graphics.color = DIVIDER_COLOR
        graphics.stroke = BasicStroke(1f)
        graphics.drawLine(40, FOOTER_START_Y + 10, IMAGE_WIDTH - 40, FOOTER_START_Y + 10)

        // Market Mood (optional)
        if (marketMood.isNotBlank()) {
            val moodFont = loadKoreanFont(15, bold = false)
            val moodText = "Market Mood  |  $marketMood"
            graphics.font = moodFont
            val moodW = graphics.fontMetrics.stringWidth(moodText)
            val moodX = (IMAGE_WIDTH - moodW) / 2
            drawText(graphics, moodText, moodX, FOOTER_START_Y + 40, moodFont, Color(0x88, 0x88, 0x88))
        }

        // few_logo.png centered
        val logoImage = loadImageResource("few_logo.png")
        if (logoImage != null) {
            val aspectRatio = logoImage.width.toDouble() / logoImage.height.toDouble()
            val (newWidth, newHeight) =
                if (logoImage.width > logoImage.height) {
                    Pair(FEW_LOGO_MAX_SIZE, (FEW_LOGO_MAX_SIZE / aspectRatio).toInt())
                } else {
                    Pair((FEW_LOGO_MAX_SIZE * aspectRatio).toInt(), FEW_LOGO_MAX_SIZE)
                }
            val resizedLogo = ImageGeneratorUtils.resizeImage(logoImage, newWidth, newHeight)
            val logoX = (IMAGE_WIDTH - newWidth) / 2
            val logoY = if (marketMood.isNotBlank()) FOOTER_START_Y + 58 else FOOTER_START_Y + 35
            graphics.drawImage(resizedLogo, logoX, logoY, null)
        }
    }

    private fun drawCompanyLogo(
        graphics: Graphics2D,
        symbol: String,
        x: Int,
        y: Int,
        size: Int,
    ) {
        val logoImage = loadImageResource("m7/${symbol.lowercase()}_logo.png")
        if (logoImage != null) {
            val aspectRatio = logoImage.width.toDouble() / logoImage.height.toDouble()
            val (newWidth, newHeight) =
                if (aspectRatio >= 1.0) {
                    Pair(size, (size / aspectRatio).toInt())
                } else {
                    Pair((size * aspectRatio).toInt(), size)
                }
            val resized = ImageGeneratorUtils.resizeImage(logoImage, newWidth, newHeight)
            val offsetX = (size - newWidth) / 2
            val offsetY = (size - newHeight) / 2
            graphics.drawImage(resized, x + offsetX, y + offsetY, null)
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
        val brandColor = BRAND_COLORS[symbol] ?: AQUA_BLUE
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

    private fun changeArrow(stock: StockQuote): Pair<String, Color> =
        when (stock.isRise) {
            true -> "▲" to RISE_COLOR
            false -> "▼" to FALL_COLOR
            null -> "-" to NEUTRAL_COLOR
        }
}

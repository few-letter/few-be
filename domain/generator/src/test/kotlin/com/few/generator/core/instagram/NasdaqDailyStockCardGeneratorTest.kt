package com.few.generator.core.instagram

import com.few.generator.core.kis.OverseaStockConstants
import com.few.generator.core.kis.StockQuote
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.LocalDate

class NasdaqDailyStockCardGeneratorTest :
    FunSpec({

        val generator = NasdaqDailyStockCardGenerator()
        val outputDir = "gen_images"

        fun etfStocks() =
            listOf(
                StockQuote(symbol = "SPY", koreanName = "S&P500", currentPrice = "525.30", changeRate = "+0.82"),
                StockQuote(symbol = "QQQ", koreanName = "나스닥100", currentPrice = "446.12", changeRate = "-0.35"),
                StockQuote(symbol = "SCHD", koreanName = "다우존스", currentPrice = "27.44", changeRate = "0.00"),
            )

        fun m7Stocks() =
            listOf(
                StockQuote(symbol = "AAPL", koreanName = "애플", currentPrice = "189.72", changeRate = "+1.23"),
                StockQuote(symbol = "MSFT", koreanName = "마이크로소프트", currentPrice = "415.80", changeRate = "+0.57"),
                StockQuote(symbol = "GOOGL", koreanName = "알파벳", currentPrice = "175.40", changeRate = "-0.91"),
                StockQuote(symbol = "AMZN", koreanName = "아마존", currentPrice = "192.15", changeRate = "+2.04"),
                StockQuote(symbol = "NVDA", koreanName = "엔비디아", currentPrice = "875.00", changeRate = "+3.41"),
                StockQuote(symbol = "META", koreanName = "메타", currentPrice = "505.22", changeRate = "-1.18"),
                StockQuote(symbol = "TSLA", koreanName = "테슬라", currentPrice = "163.57", changeRate = "0.00"),
            )

        fun fullStocks() =
            mapOf(
                OverseaStockConstants.StockGroup.ETF to etfStocks(),
                OverseaStockConstants.StockGroup.M7 to m7Stocks(),
            )

        test("더미 데이터로 나스닥 주식 카드 이미지를 생성한다") {
            val outputPath = "$outputDir/test_nasdaq_stock_card.png"

            val success = generator.generateImage(fullStocks(), outputPath)

            success shouldBe true
            val file = File(outputPath)
            file.exists() shouldBe true
            file.length() shouldBe (file.length().also { assert(it > 0L) { "이미지 파일 크기가 0입니다" } })
        }

        test("marketMood 텍스트가 포함된 이미지를 생성한다") {
            val outputPath = "$outputDir/test_nasdaq_stock_card_with_mood.png"

            val success = generator.generateImage(fullStocks(), outputPath, marketMood = "Bullish")

            success shouldBe true
            File(outputPath).exists() shouldBe true
        }

        test("marketMood 없이 이미지를 생성한다") {
            val outputPath = "$outputDir/test_nasdaq_stock_card_no_mood.png"

            val success = generator.generateImage(fullStocks(), outputPath, marketMood = "")

            success shouldBe true
            File(outputPath).exists() shouldBe true
        }

        test("특정 날짜로 헤더가 렌더링된 이미지를 생성한다") {
            val outputPath = "$outputDir/test_nasdaq_stock_card_specific_date.png"
            val fixedDate = LocalDate.of(2026, 3, 31)

            val success = generator.generateImage(fullStocks(), outputPath, date = fixedDate)

            success shouldBe true
            File(outputPath).exists() shouldBe true
        }

        test("ETF 데이터가 없어도 이미지를 생성한다") {
            val outputPath = "$outputDir/test_nasdaq_stock_card_no_etf.png"
            val stocks =
                mapOf(
                    OverseaStockConstants.StockGroup.M7 to m7Stocks(),
                )

            val success = generator.generateImage(stocks, outputPath)

            success shouldBe true
            File(outputPath).exists() shouldBe true
        }

        test("M7 데이터가 없어도 이미지를 생성한다") {
            val outputPath = "$outputDir/test_nasdaq_stock_card_no_m7.png"
            val stocks =
                mapOf(
                    OverseaStockConstants.StockGroup.ETF to etfStocks(),
                )

            val success = generator.generateImage(stocks, outputPath)

            success shouldBe true
            File(outputPath).exists() shouldBe true
        }

        test("모든 종목이 상승인 경우 이미지를 생성한다") {
            val outputPath = "$outputDir/test_nasdaq_stock_card_all_rise.png"
            val stocks =
                mapOf(
                    OverseaStockConstants.StockGroup.ETF to
                        listOf(
                            StockQuote("SPY", "S&P500", "530.00", "+1.50"),
                            StockQuote("QQQ", "나스닥100", "450.00", "+2.00"),
                            StockQuote("SCHD", "다우존스", "28.00", "+0.50"),
                        ),
                    OverseaStockConstants.StockGroup.M7 to
                        m7Stocks().map { it.copy(changeRate = "+${it.changeRate.trimStart('+', '-')}") },
                )

            val success = generator.generateImage(stocks, outputPath)

            success shouldBe true
            File(outputPath).exists() shouldBe true
        }

        test("모든 종목이 하락인 경우 이미지를 생성한다") {
            val outputPath = "$outputDir/test_nasdaq_stock_card_all_fall.png"
            val stocks =
                mapOf(
                    OverseaStockConstants.StockGroup.ETF to
                        listOf(
                            StockQuote("SPY", "S&P500", "510.00", "-1.50"),
                            StockQuote("QQQ", "나스닥100", "430.00", "-2.00"),
                            StockQuote("SCHD", "다우존스", "26.50", "-0.50"),
                        ),
                    OverseaStockConstants.StockGroup.M7 to
                        m7Stocks().map { it.copy(changeRate = "-${it.changeRate.trimStart('+', '-')}") },
                )

            val success = generator.generateImage(stocks, outputPath)

            success shouldBe true
            File(outputPath).exists() shouldBe true
        }
    })
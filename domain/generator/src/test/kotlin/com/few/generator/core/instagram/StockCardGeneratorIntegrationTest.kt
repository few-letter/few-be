package com.few.generator.core.instagram

import com.few.generator.core.kis.KisClient
import com.few.generator.core.kis.KisStockFetcher
import com.few.generator.core.kis.KisTokenClient
import com.google.gson.Gson
import feign.Feign
import feign.codec.Decoder
import feign.codec.Encoder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.cloud.openfeign.support.SpringMvcContract
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 실제 KIS API 데이터로 StockCardGenerator 이미지 생성을 검증하는 통합 테스트
 *
 * 실행 조건: 아래 환경변수가 모두 설정되어야 합니다.
 *   - KIS_APP_KEY
 *   - KIS_APP_SECRET
 *   - KIS_API_URL (선택, 기본값: https://openapi.koreainvestment.com:9443)
 *
 * 환경변수 미설정 시 테스트를 건너뜁니다.
 * 생성된 이미지는 gen_images/{yyyyMMdd}_nasdaq_stock_test.png 경로에 저장됩니다.
 */
class StockCardGeneratorIntegrationTest :
    FunSpec({

        val appKey = System.getenv("KIS_APP_KEY") ?: ""
        val appSecret = System.getenv("KIS_APP_SECRET") ?: ""
        val apiUrl = System.getenv("KIS_API_URL") ?: "https://openapi.koreainvestment.com:9443"

        val gson = Gson()
        val contract = SpringMvcContract()

        val gsonEncoder =
            Encoder { obj, _, template ->
                template.body(gson.toJson(obj))
            }
        val gsonDecoder =
            Decoder { response, type ->
                response.body().asReader(Charsets.UTF_8).use { reader ->
                    gson.fromJson(reader, type)
                }
            }

        fun buildFetcher(): KisStockFetcher {
            val tokenClient =
                Feign
                    .builder()
                    .contract(contract)
                    .encoder(gsonEncoder)
                    .decoder(gsonDecoder)
                    .target(KisTokenClient::class.java, apiUrl)

            val stockClient =
                Feign
                    .builder()
                    .contract(contract)
                    .encoder(gsonEncoder)
                    .decoder(gsonDecoder)
                    .requestInterceptor { template ->
                        template.header("appkey", appKey)
                        template.header("appsecret", appSecret)
                        template.header("Content-Type", "application/json")
                    }.target(KisClient::class.java, apiUrl)

            return KisStockFetcher(
                kisTokenClient = tokenClient,
                kisClient = stockClient,
                appKey = appKey,
                appSecret = appSecret,
            )
        }

        test("실제 KIS API 데이터로 나스닥 주식 카드 이미지를 생성한다") {
            if (appKey.isBlank() || appSecret.isBlank()) {
                println("⚠️  KIS_APP_KEY 또는 KIS_APP_SECRET 환경변수가 설정되지 않아 테스트를 건너뜁니다.")
                return@test
            }

            // 1. 실제 KIS API 조회
            val fetcher = buildFetcher()
            val stocks = fetcher.fetchAll()

            println("=== KIS API 조회 결과 (${stocks.values.sumOf { it.size }}개) ===")
            stocks.forEach { (group, groupStocks) ->
                println("--- $group ---")
                groupStocks.forEach { stock ->
                    val arrow =
                        when (stock.isRise) {
                            true -> "▲"
                            false -> "▼"
                            null -> "-"
                        }
                    println(
                        "${stock.symbol.padEnd(6)} | ${stock.koreanName.padEnd(12)} | \$${
                            stock.currentPrice.padStart(10)
                        } | $arrow ${stock.changeRate}%",
                    )
                }
            }

            // 2. 이미지 생성
            val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val outputPath = "gen_images/${dateStr}_nasdaq_stock_test.png"

            val generator = NasdaqDailyStockCardGenerator()
            val success = generator.generateImage(stocks, outputPath)

            // 3. 결과 출력 및 검증
            println("=== 이미지 생성 결과 ===")
            println("성공 여부: $success")
            println("저장 경로: ${File(outputPath).absolutePath}")

            success shouldBe true

            val outputFile = File(outputPath)
            assert(outputFile.exists()) { "이미지 파일이 생성되지 않았습니다: ${outputFile.absolutePath}" }
            assert(outputFile.length() > 0L) { "이미지 파일 크기가 0입니다: ${outputFile.absolutePath}" }

            println("파일 크기: ${outputFile.length()} bytes")
        }
    })

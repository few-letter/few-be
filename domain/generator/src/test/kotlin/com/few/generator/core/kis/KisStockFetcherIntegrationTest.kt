package com.few.generator.core.kis

import com.google.gson.Gson
import feign.Feign
import feign.codec.Decoder
import feign.codec.Encoder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.string.shouldNotBeBlank
import org.springframework.cloud.openfeign.support.SpringMvcContract

/**
 * 실제 KIS API를 호출하는 통합 테스트
 *
 * 실행 조건: 아래 환경변수가 모두 설정되어야 합니다.
 *   - KIS_APP_KEY
 *   - KIS_APP_SECRET
 *   - KIS_API_URL (선택, 기본값: https://openapi.koreainvestment.com:9443)
 *
 * 환경변수 미설정 시 테스트를 건너뜁니다.
 */
class KisStockFetcherIntegrationTest :
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

        test("실제 KIS API로 Nasdaq 전체 종목 데이터를 조회한다") {
            if (appKey.isBlank() || appSecret.isBlank()) {
                println("⚠️  KIS_APP_KEY 또는 KIS_APP_SECRET 환경변수가 설정되지 않아 테스트를 건너뜁니다.")
                return@test
            }

            val fetcher = buildFetcher()
            val result = fetcher.fetchAll()

            println("=== KIS API 조회 결과 (${result.size}개) ===")
            result.forEach { stock ->
                val arrow =
                    when (stock.isRise) {
                        true -> "▲"
                        false -> "▼"
                        null -> "-"
                    }
                println(
                    "${stock.symbol.padEnd(
                        6,
                    )} | ${stock.koreanName.padEnd(10)} | \$${stock.currentPrice.padStart(10)} | $arrow ${stock.changeRate}%",
                )
            }

            result.shouldNotBeEmpty()
            result.forEach { stock ->
                stock.symbol.shouldNotBeBlank()
                stock.currentPrice.shouldNotBeBlank()
                stock.changeRate.shouldNotBeBlank()
            }
        }
    })
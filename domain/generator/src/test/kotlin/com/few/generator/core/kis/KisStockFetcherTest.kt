package com.few.generator.core.kis

import com.few.generator.core.kis.dto.KisStockPriceResponse
import com.few.generator.core.kis.dto.KisTokenResponse
import feign.FeignException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class KisStockFetcherTest :
    BehaviorSpec({

        val kisTokenClient = mockk<KisTokenClient>()
        val kisClient = mockk<KisClient>()
        val appKey = "test-app-key"
        val appSecret = "test-app-secret"

        val fetcher =
            KisStockFetcher(
                kisTokenClient = kisTokenClient,
                kisClient = kisClient,
                appKey = appKey,
                appSecret = appSecret,
            )

        val mockTokenResponse =
            KisTokenResponse(
                accessToken = "mock-access-token",
                tokenType = "Bearer",
                expiresIn = 86400L,
            )

        fun mockStockResponse(
            changeRate: String = "+1.00",
            last: String = "100.00",
        ) = KisStockPriceResponse(
            rtCd = "0",
            msg1 = "정상처리 되었습니다.",
            output =
                KisStockPriceResponse.Output(
                    last = last,
                    t_xrat = changeRate,
                ),
        )

        // 모든 종목을 주어진 changeRate로 세팅
        fun stubAllStocks(changeRate: String = "+1.00") {
            NasdaqStockConstants.ALL_STOCKS.forEach { stock ->
                every {
                    kisClient.getStockPrice(
                        authorization = any(),
                        trId = any(),
                        excd = stock.excd,
                        symb = stock.symbol,
                    )
                } returns mockStockResponse(changeRate = changeRate)
            }
        }

        // AAPL만 다른 changeRate, 나머지는 보합으로 세팅
        fun stubAllStocksWithAaplRate(aaplRate: String) {
            NasdaqStockConstants.ALL_STOCKS.forEach { stock ->
                val rate = if (stock.symbol == "AAPL") aaplRate else "0.00"
                every {
                    kisClient.getStockPrice(
                        authorization = any(),
                        trId = any(),
                        excd = stock.excd,
                        symb = stock.symbol,
                    )
                } returns mockStockResponse(changeRate = rate)
            }
        }

        // 매 Then 실행 전 모든 mock 초기화 (stub 누적 방지)
        beforeEach { clearAllMocks() }

        Given("토큰 발급과 전체 종목 조회가 모두 성공할 때") {
            beforeEach {
                every { kisTokenClient.getToken(any()) } returns mockTokenResponse
                stubAllStocks()
            }

            When("fetchAll()을 호출하면") {
                Then("전체 종목 수만큼 결과를 반환한다") {
                    val result = fetcher.fetchAll()
                    result shouldHaveSize NasdaqStockConstants.ALL_STOCKS.size
                }

                Then("토큰 발급을 1회만 호출한다") {
                    fetcher.fetchAll()
                    verify(exactly = 1) { kisTokenClient.getToken(any()) }
                }

                Then("AAPL 데이터가 올바르게 매핑된다") {
                    val result = fetcher.fetchAll()
                    val aapl = result.first { it.symbol == "AAPL" }
                    aapl.koreanName shouldBe "애플"
                    aapl.currentPrice shouldBe "100.00"
                    aapl.changeRate shouldBe "+1.00"
                }
            }
        }

        Given("changeRate가 '+'로 시작할 때") {
            beforeEach {
                every { kisTokenClient.getToken(any()) } returns mockTokenResponse
                stubAllStocksWithAaplRate("+1.00")
            }

            When("fetchAll()을 호출하면") {
                Then("AAPL의 isRise가 true이다") {
                    val result = fetcher.fetchAll()
                    result.first { it.symbol == "AAPL" }.isRise shouldBe true
                }
            }
        }

        Given("changeRate가 '-'로 시작할 때") {
            beforeEach {
                every { kisTokenClient.getToken(any()) } returns mockTokenResponse
                stubAllStocksWithAaplRate("-1.00")
            }

            When("fetchAll()을 호출하면") {
                Then("AAPL의 isRise가 false이다") {
                    val result = fetcher.fetchAll()
                    result.first { it.symbol == "AAPL" }.isRise shouldBe false
                }
            }
        }

        Given("changeRate가 부호 없이 '0'일 때") {
            beforeEach {
                every { kisTokenClient.getToken(any()) } returns mockTokenResponse
                stubAllStocks("0.00")
            }

            When("fetchAll()을 호출하면") {
                Then("AAPL의 isRise가 null이다") {
                    val result = fetcher.fetchAll()
                    result.first { it.symbol == "AAPL" }.isRise.shouldBeNull()
                }
            }
        }

        Given("일부 종목 API 호출이 FeignException으로 실패할 때") {
            beforeEach {
                every { kisTokenClient.getToken(any()) } returns mockTokenResponse
                NasdaqStockConstants.ALL_STOCKS.forEachIndexed { index, stock ->
                    if (index % 2 == 0) {
                        every {
                            kisClient.getStockPrice(any(), any(), stock.excd, stock.symbol)
                        } returns mockStockResponse()
                    } else {
                        every {
                            kisClient.getStockPrice(any(), any(), stock.excd, stock.symbol)
                        } throws RuntimeException("simulated feign error")
                    }
                }
            }

            When("fetchAll()을 호출하면") {
                Then("성공한 종목만 반환한다") {
                    val result = fetcher.fetchAll()
                    val expectedCount = NasdaqStockConstants.ALL_STOCKS.filterIndexed { index, _ -> index % 2 == 0 }.size
                    result shouldHaveSize expectedCount
                }
            }
        }

        Given("모든 종목이 rt_cd 실패 응답을 반환할 때") {
            beforeEach {
                every { kisTokenClient.getToken(any()) } returns mockTokenResponse
                NasdaqStockConstants.ALL_STOCKS.forEach { stock ->
                    every {
                        kisClient.getStockPrice(any(), any(), stock.excd, stock.symbol)
                    } returns KisStockPriceResponse(rtCd = "1", msg1 = "오류", output = null)
                }
            }

            When("fetchAll()을 호출하면") {
                Then("빈 리스트를 반환한다") {
                    val result = fetcher.fetchAll()
                    result shouldHaveSize 0
                }
            }
        }

        Given("토큰 발급이 실패할 때") {
            beforeEach {
                every {
                    kisTokenClient.getToken(any())
                } throws mockk<FeignException>(relaxed = true)
            }

            When("fetchAll()을 호출하면") {
                Then("FeignException이 전파된다") {
                    shouldThrow<FeignException> {
                        fetcher.fetchAll()
                    }
                }
            }
        }
    })
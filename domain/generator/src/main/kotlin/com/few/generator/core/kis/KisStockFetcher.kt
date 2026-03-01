package com.few.generator.core.kis

import com.few.generator.core.kis.dto.KisTokenRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KisStockFetcher(
    private val kisTokenClient: KisTokenClient,
    private val kisClient: KisClient,
    @Value("\${KIS_APP_KEY:thisis-kis-app-key}")
    private val appKey: String,
    @Value("\${KIS_APP_SECRET:thisis-kis-app-secret}")
    private val appSecret: String,
) {
    private val log = KotlinLogging.logger {}

    fun fetchAll(): Map<NasdaqStockConstants.StockGroup, List<NasdaqStockData>> {
        val accessToken = issueToken()
        val authorization = "Bearer $accessToken"

        return NasdaqStockConstants.STOCK_GROUP_MAP.mapValues { (_, stocks) ->
            stocks.mapNotNull { stock ->
                runCatching {
                    val response =
                        kisClient.getStockPrice(
                            authorization = authorization,
                            trId = NasdaqStockConstants.OVERSEA_PRICE_DETAIL_TR_ID,
                            excd = stock.excd,
                            symb = stock.symbol,
                        )

                    if (!response.isSuccess() || response.output == null) {
                        log.warn { "[${stock.symbol}] KIS API 응답 실패: rt_cd=${response.rtCd}, msg=${response.msg1}" }
                        return@runCatching null
                    }

                    val output = response.output

                    NasdaqStockData(
                        symbol = stock.symbol,
                        koreanName = stock.koreanName,
                        currentPrice = output.last,
                        changeRate = output.t_xrat,
                    )
                }.onFailure { e ->
                    log.error(e) { "[${stock.symbol}] KIS 주식 데이터 조회 실패: ${e.message}" }
                }.getOrNull()
            }
        }
    }

    private fun issueToken(): String {
        val response =
            kisTokenClient.getToken(
                KisTokenRequest(
                    appKey = appKey,
                    appSecret = appSecret,
                ),
            )
        return response.accessToken
    }
}
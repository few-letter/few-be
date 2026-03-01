package com.few.generator.core.kis

object NasdaqStockConstants {
    const val OVERSEA_PRICE_DETAIL_TR_ID = "HHDFS76200200"

    /** KIS 거래소 코드 */
    const val EXCD_NAS = "NAS" // 나스닥
    const val EXCD_AMS = "AMS" // 아멕스

    enum class StockGroup { M7, ETF }

    data class NasdaqStock(
        val symbol: String,
        val koreanName: String,
        val excd: String,
    )

    /** M7 개별 종목 */
    val AAPL = NasdaqStock("AAPL", "애플", EXCD_NAS)
    val MSFT = NasdaqStock("MSFT", "마이크로소프트", EXCD_NAS)
    val GOOGL = NasdaqStock("GOOGL", "알파벳", EXCD_NAS)
    val AMZN = NasdaqStock("AMZN", "아마존", EXCD_NAS)
    val NVDA = NasdaqStock("NVDA", "엔비디아", EXCD_NAS)
    val META = NasdaqStock("META", "메타", EXCD_NAS)
    val TSLA = NasdaqStock("TSLA", "테슬라", EXCD_NAS)

    /** ETF 개별 종목 */
    val SPY = NasdaqStock("SPY", "S&P500", EXCD_AMS)
    val QQQ = NasdaqStock("QQQ", "나스닥100", EXCD_NAS)
    val SCHD = NasdaqStock("SCHD", "미국배당다우존스", EXCD_AMS)


    val STOCK_GROUP_MAP: Map<StockGroup, List<NasdaqStock>> =
        mapOf(
            StockGroup.ETF to listOf(SPY, QQQ, SCHD),
            StockGroup.M7 to listOf(AAPL, MSFT, GOOGL, AMZN, NVDA, META, TSLA),
        )

}